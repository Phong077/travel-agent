import { reactive } from 'vue'

export const apiStatus = reactive({
  usingMock: false,
  message: '',
})

export function markApiReady() {
  apiStatus.usingMock = false
  apiStatus.message = ''
}

export function markApiFallback(message: string) {
  apiStatus.usingMock = true
  apiStatus.message = message
}
