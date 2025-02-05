const { merge } = require('webpack-merge');
const common = require('./webpack.common.js');
const Dotenv = require('dotenv-webpack');
const webpack = require('webpack');

module.exports = merge(common, {
  mode: 'development',
  devtool: 'eval',
  devServer: {
    historyApiFallback: true,
    port: 3000,
    hot: true,
    open: true,
  },
  plugins: [
    new Dotenv({ path: './.env.development' }),
    new webpack.DefinePlugin({
      PRODUCT_ENV: JSON.stringify(process.env.NODE_ENV),
      MOCKING_ENV: JSON.stringify(process.env.MOCKING_ENV),
    }),
  ],
});
