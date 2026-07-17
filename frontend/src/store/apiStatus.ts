import { reactive } from 'vue'

export const apiStatus = reactive({
  checked: false,
  usingMock: false,
  message: '',
})

export function markApiReady() {
  apiStatus.checked = true
  apiStatus.usingMock = false
  apiStatus.message = '已连接后端真实接口，当前数据来自 Spring Boot 服务。'
}

export function markApiFallback(message: string) {
  apiStatus.checked = true
  apiStatus.usingMock = true
  apiStatus.message = message
}
