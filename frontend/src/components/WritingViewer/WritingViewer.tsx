import DOMPurify from 'dompurify';
import hljs from 'highlight.js';
import { useEffect } from 'react';
import { styled } from 'styled-components';
import { getWriting } from 'apis/writings';
import Divider from 'components/@common/Divider/Divider';
import { useGetQuery } from 'hooks/@common/useGetQuery';
import { GetWritingResponse } from 'types/apis/writings';

type Props = { writingId: number };

const WritingViewer = ({ writingId }: Props) => {
  const { data, isLoading, getData } = useGetQuery<GetWritingResponse>({
    fetcher: () => getWriting(writingId),
    // onSuccess: () => hljs.highlightAll(),
  });

  useEffect(() => {
    const refetch = async () => {
      await getData();
    };
    refetch();
  }, [writingId]);

  useEffect(() => {
    hljs.highlightAll();
  }, [data]);

  if (isLoading) return <div>로딩 중...</div>;

  return (
    <S.WritingViewerContainer>
      <S.TitleWrapper>
        <S.Title>{data?.title}</S.Title>
      </S.TitleWrapper>
      <Divider />
      <S.ContentWrapper
        dangerouslySetInnerHTML={{
          __html: DOMPurify.sanitize(data?.content ?? '글 내용이 없습니다'),
        }}
      />
    </S.WritingViewerContainer>
  );
};

export default WritingViewer;

const S = {
  WritingViewerContainer: styled.section`
    padding: 8rem 4rem;
    max-width: 100%;
    overflow-wrap: break-word;
  `,
  TitleWrapper: styled.div`
    padding-bottom: 2rem;
  `,
  Title: styled.h1`
    font-size: 4rem;
  `,
  ContentWrapper: styled.section`
    padding: 1.6rem 0;
    font-size: 1.6rem;

    h1 {
      margin: 3.4rem 0 1.7rem;
      font-size: 3.4rem;
    }

    h2 {
      margin: 2.8rem 0 1.4rem;
      font-size: 2.8rem;
    }

    h3 {
      margin: 2.2rem 0 1.1rem;
      font-size: 2.2rem;
    }

    h4 {
      margin: 1.6rem 0 0.8rem;
      font-size: 1.6rem;
    }

    h5 {
      margin: 1.3rem 0 0.65rem;
      font-size: 1.3rem;
    }

    h6 {
      margin: 1rem 0 0.5rem;
      font-size: 1rem;
    }

    p {
      margin: 1.6rem 0;
      font-size: 1.6rem;
    }

    blockquote {
      padding: 1rem 2rem;
      margin: 1.6rem 0;
      border-left: 4px solid ${({ theme }) => theme.color.gray8};
      background-color: ${({ theme }) => theme.color.gray2};
      color: ${({ theme }) => theme.color.gray9};
      line-height: 2.4rem;
    }

    ol,
    ul {
      padding-left: 2rem;
      margin: 1rem 0;
    }

    ul > li {
      list-style: disc;
    }

    ol > li {
      list-style: decimal;
    }

    li {
      margin-bottom: 10px;
    }

    a {
      color: #0968da;
      text-decoration: underline;
      &:visited {
        color: #0968da;
      }
    }

    code {
      padding: 0.2rem 0.4rem;
      margin: 0.1rem;
      border: none solid #eee;
      border-radius: 4px;
      background-color: ${({ theme }) => theme.color.gray4};
      color: #eb5756;
    }

    pre > code {
      color: inherit;
    }

    img {
      max-width: 100%;
      height: auto;
    }

    strong {
      font-weight: bold;
    }

    em {
      font-style: italic;
    }
  `,
};
