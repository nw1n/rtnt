const fs = require('node:fs')
const path = require('node:path')

const apiBaseUrl = process.env.API_BASE_URL || 'http://localhost:18080/api'
const version = process.env.APP_VERSION || 'local'

const outputPath = path.join(__dirname, '..', 'src', 'assets', 'env.js')

const content = `window.__env = {
  version: '${version}',
  API_BASE_URL: '${apiBaseUrl}',
}
`

fs.writeFileSync(outputPath, content, 'utf8')
console.log(`Generated ${outputPath} with API_BASE_URL=${apiBaseUrl}`)
