export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface UserResponse {
  id: string
  username: string
  email?: string
  status: string
  createdAt: string
  updatedAt: string
}

export interface LoginResponse {
  token: string
  user: UserResponse
}

export interface KnowledgeBaseResponse {
  id: string
  name: string
  description?: string
  status: string
  createdAt: string
  updatedAt: string
}

export interface DocumentResponse {
  id: string
  knowledgeBaseId: string
  originalFilename: string
  contentType: string
  fileSize: number
  status: string
  chunkCount: number
  errorMessage?: string
  createdAt: string
  updatedAt: string
}

export interface ConversationResponse {
  id: string
  title: string
  status: string
  knowledgeBaseId: string
  createdAt: string
  updatedAt: string
}

export interface ChatReference {
  documentId: string
  fileName: string
  chunkIndex: number
  content: string
  score: number
}

export interface ChatResponse {
  answer: string
  references: ChatReference[]
  userMessageId?: string
  assistantMessageId?: string
  invocationLogId?: string
}

export interface InvocationLogResponse {
  id: string
  knowledgeBaseId?: string
  conversationId?: string
  messageId?: string
  modelName: string
  promptTokens: number
  completionTokens: number
  totalTokens: number
  durationMs: number
  status: string
  errorMessage?: string
  createdAt: string
}
