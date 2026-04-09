const baseConfig = require('@elderbyte/code-style/src/prettier/elder-prettier-config.js')

module.exports = {
  ...baseConfig,
  overrides: [
    ...(baseConfig.overrides || []),
    {
      files: 'src/**/*.html',
      options: {
        parser: 'angular',
      },
    },
  ],
}
