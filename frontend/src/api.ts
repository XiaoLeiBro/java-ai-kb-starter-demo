import axios from 'axios'
import type {
  ApiResult,
  ChatResponse,
  ConversationResponse,
  DocumentResponse,
  InvocationLogResponse,
  KnowledgeBaseResponse,
  LoginResponse,
  UserResponse,
} from './types'

const TOKEN_KEY = 'ai-kb-demo-token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

const client = axios.create({
  baseURL: '/api/v1',
  timeout: 60000,
})

const errorCodeMessages: Record<string, string> = {
  USERNAME_EXISTS: '用户名已存在，请换一个用户名',
  INVALID_CREDENTIALS: '用户名或密码错误',
  UNAUTHORIZED: '登录已过期，请重新登录',
  VALIDATION_ERROR: '请求参数不正确',
  UNSUPPORTED_FILE_TYPE: '不支持的文件类型',
  NOT_FOUND: '资源不存在或无权访问',
  LLM_PROVIDER_ERROR: '模型服务调用失败，请稍后重试',
  CONVERSATION_ARCHIVED: '该会话已归档，不能继续发送消息',
  CONVERSATION_GONE: '该会话已归档，消息不可继续查看',
  KB_MISMATCH: '会话和当前知识库不匹配',
  KNOWLEDGE_BASE_ARCHIVED: '该知识库已归档，不能继续使用',
  INTERNAL_ERROR: '系统内部错误，请稍后重试',
}

function translateBackendMessage(message?: string) {
  if (!message) return '请求失败'

  const codeMatch = message.match(/^([A-Z_]+)(?::\s*(.*))?$/)
  if (codeMatch) {
    const [, code, detail] = codeMatch
    const translated = errorCodeMessages[code]
    if (translated) {
      if (!detail) return translated
      return translateDetailMessage(detail, translated)
    }
  }

  return translateDetailMessage(message, message)
}

function translateDetailMessage(detail: string, fallback: string) {
  if (detail.includes('timeout of') && detail.includes('exceeded')) {
    return '请求超时：模型服务响应过慢或网络不可用，请稍后重试'
  }
  if (detail.includes('Network Error')) return '网络连接失败，请检查服务是否已启动'
  if (detail.includes('Invalid username or password')) return '用户名或密码错误'
  if (detail.includes('Username already exists')) return '用户名已存在，请换一个用户名'
  if (detail.includes('Authentication required')) return '请先登录'
  if (detail.includes('Unsupported file type')) {
    const ext = detail.match(/Unsupported file type: (.+)$/)?.[1]
    return ext ? `不支持的文件类型：${ext}` : '不支持的文件类型'
  }
  if (detail.includes('PDF 未提取到文本')) return detail
  if (detail.includes('PDF 文件解析失败')) return detail
  if (detail.includes('不支持加密 PDF')) return detail
  if (detail.includes('文件为空')) return '文件为空，请选择有效文件'
  if (detail.includes('文件过大')) return '文件过大，请选择更小的文件'
  if (detail.includes('Resource not found')) return '资源不存在或无权访问'
  if (detail.includes('Validation failed')) return '请求参数不正确，请检查输入内容'
  if (detail.includes('LLM provider call failed')) return '模型服务调用失败，请稍后重试'
  if (detail.includes('Error while extracting response')) return '模型服务响应异常，请检查模型配置或网络连接'
  if (detail.includes('Internal server error')) return '系统内部错误，请稍后重试'
  return fallback
}

client.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

function messageFromError(error: unknown) {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as Partial<ApiResult<unknown>> | undefined
    return translateBackendMessage(data?.message || error.message)
  }
  return '请求失败'
}

async function unwrap<T>(request: Promise<{ data: ApiResult<T> }>): Promise<T> {
  try {
    const response = await request
    if (response.data.code !== 200) {
      throw new Error(translateBackendMessage(response.data.message))
    }
    return response.data.data
  } catch (error) {
    throw new Error(messageFromError(error))
  }
}

export const api = {
  async health() {
    const response = await axios.get('/actuator/health')
    return response.data as { status: string; groups?: string[] }
  },
  register(payload: { username: string; password: string; email?: string }) {
    return unwrap<UserResponse>(client.post('/auth/register', payload))
  },
  login(payload: { username: string; password: string }) {
    return unwrap<LoginResponse>(client.post('/auth/login', payload))
  },
  me() {
    return unwrap<UserResponse>(client.get('/auth/me'))
  },
  listKnowledgeBases() {
    return unwrap<KnowledgeBaseResponse[]>(client.get('/knowledge-bases'))
  },
  createKnowledgeBase(payload: { name: string; description?: string }) {
    return unwrap<KnowledgeBaseResponse>(client.post('/knowledge-bases', payload))
  },
  listDocuments(knowledgeBaseId: string) {
    return unwrap<DocumentResponse[]>(client.get(`/knowledge-bases/${knowledgeBaseId}/documents`))
  },
  uploadDocument(knowledgeBaseId: string, file: File) {
    const form = new FormData()
    form.append('file', file)
    return unwrap<DocumentResponse>(
      client.post(`/knowledge-bases/${knowledgeBaseId}/documents`, form, {
        headers: { 'Content-Type': 'multipart/form-data' },
      }),
    )
  },
  async downloadDocument(knowledgeBaseId: string, documentId: string) {
    const response = await client.get(`/knowledge-bases/${knowledgeBaseId}/documents/${documentId}/download`, {
      responseType: 'blob',
    })
    return response.data as Blob
  },
  createConversation(payload: { knowledgeBaseId: string; title?: string }) {
    return unwrap<ConversationResponse>(client.post('/conversations', payload))
  },
  listConversations(knowledgeBaseId?: string) {
    return unwrap<ConversationResponse[]>(
      client.get('/conversations', { params: { knowledgeBaseId: knowledgeBaseId || undefined } }),
    )
  },
  chat(payload: { knowledgeBaseId: string; question: string; topK: number; conversationId?: string }) {
    return unwrap<ChatResponse>(client.post('/chat', payload))
  },
  listInvocationLogs(params: { knowledgeBaseId?: string; dateFrom?: string; dateTo?: string }) {
    return unwrap<InvocationLogResponse[]>(client.get('/invocation-logs', { params }))
  },
}
