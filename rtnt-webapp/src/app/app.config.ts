import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http'
import { ApplicationConfig, importProvidersFrom } from '@angular/core'
import { provideRouter } from '@angular/router'
import {
  ElderLocalesDeChModule,
  ElderThemeModule,
  ElderTimeModule,
  provideElderDefaults,
  provideElderLanguage,
  provideElderTranslate,
} from '@elderbyte/ngx-starter'
import { routes } from './app.routes'

export const appConfig: ApplicationConfig = {
  providers: [
    provideElderDefaults(),
    provideElderLanguage({ langs: ['en'], defaultLang: 'en' }),
    provideElderTranslate(),
    importProvidersFrom(ElderLocalesDeChModule.forRoot(), ElderTimeModule, ElderThemeModule),
    provideHttpClient(withInterceptorsFromDi()),
    provideRouter(routes),
  ],
}
