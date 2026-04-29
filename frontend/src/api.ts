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
    return data?.message || error.message || '请求失败'
  }
  return '请求失败'
}

async function unwrap<T>(request: Promise<{ data: ApiResult<T> }>): Promise<T> {
  try {
    const response = await request
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
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
