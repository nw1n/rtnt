import baseConfig from './node_modules/@elderbyte/code-style/src/eslint/.eslint.config.mjs'

export default [
  // Global ignores (replaces .eslintignore)
  {
    ignores: ['node_modules/**', 'dist/**', '.angular/**', '.idea/**'],
  },

  // Extend the base configuration
  ...baseConfig,

  {
    rules: {
      'no-console': 'ignore',
    },
  },
]
