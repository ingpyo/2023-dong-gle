package org.donggle.backend.application.service;

import lombok.RequiredArgsConstructor;
import org.donggle.backend.application.repository.CategoryRepository;
import org.donggle.backend.application.repository.MemberRepository;
import org.donggle.backend.application.repository.WritingRepository;
import org.donggle.backend.application.service.request.CategoryAddRequest;
import org.donggle.backend.application.service.request.CategoryModifyRequest;
import org.donggle.backend.domain.category.Category;
import org.donggle.backend.domain.category.CategoryName;
import org.donggle.backend.domain.member.Member;
import org.donggle.backend.domain.writing.Writing;
import org.donggle.backend.exception.business.InvalidBasicCategoryException;
import org.donggle.backend.exception.notfound.CategoryNotFoundException;
import org.donggle.backend.exception.notfound.MemberNotFoundException;
import org.donggle.backend.ui.response.CategoryListResponse;
import org.donggle.backend.ui.response.CategoryResponse;
import org.donggle.backend.ui.response.CategoryWritingsResponse;
import org.donggle.backend.ui.response.WritingSimpleResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {
    private static final int LAST_WRITING_FLAG = -1;
    private static final int FIRST_WRITING_INDEX = 0;

    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final WritingRepository writingRepository;

    public Long addCategory(final Long memberId, final CategoryAddRequest request) {
        //TODO: member checking
        final Member findMember = findMember(memberId);
        final Category category = Category.of(new CategoryName(request.categoryName()), findMember);
        final Category lastCategory = findLastCategoryByMemberId(memberId);
        final Category savedCategory = categoryRepository.save(category);
        lastCategory.changeNextCategory(savedCategory);
        return savedCategory.getId();
    }

    @Transactional(readOnly = true)
    public CategoryListResponse findAll(final Long memberId) {
        //TODO: member checking
        final List<Category> categories = categoryRepository.findAllByMemberId(memberId);
        final List<Category> sortedCategories = sortCategory(categories, findBasicCategoryByMemberId(memberId));
        final List<CategoryResponse> categoryResponses = sortedCategories.stream()
                .map(CategoryResponse::of)
                .toList();
        return CategoryListResponse.from(categoryResponses);
    }

    private List<Category> sortCategory(final List<Category> categories, Category targetCategory) {
        final Map<Category, Category> categoryMap = new LinkedHashMap<>();
        for (final Category category : categories) {
            categoryMap.put(category, category.getNextCategory());
        }
        final List<Category> sortedCategories = new ArrayList<>();
        sortedCategories.add(targetCategory);
        while (Objects.nonNull(targetCategory.getNextCategory())) {
            targetCategory = categoryMap.get(targetCategory);
            sortedCategories.add(targetCategory);
        }
        return sortedCategories;
    }

    @Transactional(readOnly = true)
    public CategoryWritingsResponse findAllWritings(final Long memberId, final Long categoryId) {
        //TODO: member checking
        final Category findCategory = findCategory(categoryId);
        final List<Writing> findWritings = writingRepository.findAllByCategoryId(findCategory.getId());
        if (findWritings.isEmpty()) {
            return CategoryWritingsResponse.of(findCategory, Collections.emptyList());
        }
        final Writing firstWriting = findFirstWriting(findWritings);
        final List<Writing> sortedWriting = sortWriting(findWritings, firstWriting);
        final List<WritingSimpleResponse> writingSimpleResponses = sortedWriting.stream()
                .map(WritingSimpleResponse::from)
                .toList();
        return CategoryWritingsResponse.of(findCategory, writingSimpleResponses);
    }

    private Writing findFirstWriting(final List<Writing> findWritings) {
        final List<Writing> copy = new ArrayList<>(findWritings);
        final List<Writing> nextWritings = findWritings.stream()
                .map(Writing::getNextWriting)
                .toList();
        copy.removeAll(nextWritings);
        return copy.get(FIRST_WRITING_INDEX);
    }

    private List<Writing> sortWriting(final List<Writing> findWritings, Writing targetWriting) {
        final Map<Writing, Writing> writingMap = new LinkedHashMap<>();
        for (final Writing findWriting : findWritings) {
            writingMap.put(findWriting, findWriting.getNextWriting());
        }
        final List<Writing> sortedWriting = new ArrayList<>();
        sortedWriting.add(targetWriting);
        while (Objects.nonNull(targetWriting.getNextWriting())) {
            targetWriting = writingMap.get(targetWriting);
            sortedWriting.add(targetWriting);
        }
        return sortedWriting;
    }

    public void modifyCategoryName(final Long memberId, final Long categoryId, final CategoryModifyRequest request) {
        //TODO: member checking
        final Category findCategory = findCategory(categoryId);
        validateBasicCategory(findCategory);
        findCategory.changeName(new CategoryName(request.categoryName()));
    }

    public void removeCategory(final Long memberId, final Long categoryId) {
        //TODO: member checking
        final Category findCategory = findCategory(categoryId);
        validateBasicCategory(findCategory);
        transferToBasicCategory(memberId, findCategory);
        deleteCategory(findCategory);
    }

    private void transferToBasicCategory(final Long memberId, final Category findCategory) {
        final Category basicCategory = findBasicCategoryByMemberId(memberId);
        if (haveWritingsCategory(findCategory)) {
            final List<Writing> findWritings = writingRepository.findAllByCategoryId(findCategory.getId());
            final Writing firstWritingInCategory = findFirstWriting(findWritings);
            if (haveWritingsCategory(basicCategory)) {
                final Writing lastWritingInBasicCategory = findLastWritingInCategory(basicCategory);
                lastWritingInBasicCategory.changeNextWriting(firstWritingInCategory);
            }
            findWritings.forEach(writing -> writing.changeCategory(basicCategory));
        }
    }

    private boolean haveWritingsCategory(final Category category) {
        return !writingRepository.findAllByCategoryId(category.getId()).isEmpty();
    }

    private void deleteCategory(final Category findCategory) {
        final Category nextCategory = findCategory.getNextCategory();
        final Category preCategory = findPreCategory(findCategory.getId());
        findCategory.changeNextCategoryNull();
        categoryRepository.flush();
        preCategory.changeNextCategory(nextCategory);
        categoryRepository.delete(findCategory);
    }

    public void modifyCategoryOrder(final Long memberId, final Long categoryId, final CategoryModifyRequest request) {
        final Long nextCategoryId = request.nextCategoryId();
        final Category source = findCategory(categoryId);
        validateBasicCategory(source);
        deleteCategoryOrder(source);
        addCategoryOrder(nextCategoryId, source, memberId);
    }

    private void deleteCategoryOrder(final Category category) {
        final Category nextCategory = category.getNextCategory();
        category.changeNextCategoryNull();
        final Category preCategory = findPreCategory(category.getId());
        preCategory.changeNextCategory(nextCategory);
    }

    private void addCategoryOrder(final Long nextCategoryId, final Category category, final Long memberId) {
        final Category preCategory;
        if (nextCategoryId == LAST_WRITING_FLAG) {
            preCategory = findLastCategoryByMemberId(memberId);
        } else {
            preCategory = findPreCategory(nextCategoryId);
        }
        preCategory.changeNextCategory(category);
        if (nextCategoryId != LAST_WRITING_FLAG) {
            final Category nextCategory = findCategory(nextCategoryId);
            category.changeNextCategory(nextCategory);
        }
    }

    private void validateBasicCategory(final Category category) {
        if (category.isBasic()) {
            throw new InvalidBasicCategoryException();
        }
    }

    private Category findLastCategoryByMemberId(final Long memberId) {
        return categoryRepository.findLastCategoryByMemberId(memberId)
                .orElseThrow(IllegalStateException::new);
    }

    private Category findBasicCategoryByMemberId(final Long memberId) {
        return categoryRepository.findFirstByMemberId(memberId)
                .orElseThrow(IllegalStateException::new);
    }

    private Category findPreCategory(final Long categoryId) {
        return categoryRepository.findPreCategoryByCategoryId(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }

    private Writing findLastWritingInCategory(final Category findCategory) {
        return writingRepository.findLastWritingByCategoryId(findCategory.getId())
                .orElseThrow(IllegalStateException::new);
    }

    private Member findMember(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
    }

    private Category findCategory(final Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }
}
