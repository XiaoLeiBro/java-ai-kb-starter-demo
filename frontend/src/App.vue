<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  ChatDotRound,
  CircleCheck,
  Collection,
  DataAnalysis,
  Download,
  DocumentAdd,
  Finished,
  Key,
  Link,
  Lock,
  Refresh,
  Search,
  SwitchButton,
  UploadFilled,
  User,
} from '@element-plus/icons-vue'
import { ElMessage, type UploadRequestOptions } from 'element-plus'
import { api, clearToken, getToken, setToken } from './api'
import type {
  ChatReference,
  ChatResponse,
  ConversationResponse,
  DocumentResponse,
  InvocationLogResponse,
  KnowledgeBaseResponse,
  UserResponse,
} from './types'

type Workspace = 'knowledge' | 'chat' | 'logs'

const loading = ref(false)
const booting = ref(true)
const activeWorkspace = ref<Workspace>('knowledge')
const currentUser = ref<UserResponse | null>(null)
const healthStatus = ref('UNKNOWN')
const knowledgeBases = ref<KnowledgeBaseResponse[]>([])
const documents = ref<DocumentResponse[]>([])
const conversations = ref<ConversationResponse[]>([])
const logs = ref<InvocationLogResponse[]>([])
const selectedKnowledgeBaseId = ref('')
const selectedConversationId = ref('')
const chatResult = ref<ChatResponse | null>(null)
const chatHistory = ref<Array<{ role: 'user' | 'assistant'; content: string; references?: ChatReference[] }>>([])

const authMode = ref<'login' | 'register'>('login')
const authForm = reactive({
  username: 'demo_user',
  password: 'demo_password',
  email: 'demo@example.com',
})

const knowledgeForm = reactive({
  name: '公司制度知识库',
  description: '用于回答员工手册、报销制度、请假制度等问题',
})

const chatForm = reactive({
  question: '员工报销发票有什么要求？',
  topK: 5,
  useConversation: true,
})

const logFilter = reactive({
  knowledgeBaseId: '',
  dateFrom: '',
  dateTo: '',
})

const selectedKnowledgeBase = computed(() =>
  knowledgeBases.value.find((item) => item.id === selectedKnowledgeBaseId.value),
)

const selectedDocuments = computed(() =>
  documents.value.filter((item) => item.knowledgeBaseId === selectedKnowledgeBaseId.value),
)

const readyDocumentCount = computed(
  () => documents.value.filter((item) => item.status === 'READY').length,
)

const latestLogs = computed(() => logs.value.slice(0, 8))

const averageDurationSeconds = computed(() => {
  if (!logs.value.length) return '0.00'
  const averageMs = logs.value.reduce((sum, item) => sum + item.durationMs, 0) / logs.value.length
  return formatDurationSeconds(averageMs)
})

function shortId(value?: string) {
  if (!value) return '-'
  return value.length > 8 ? `${value.slice(0, 8)}...` : value
}

function formatTime(value?: string) {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

function formatDate(value?: string) {
  if (!value) return '-'
  return new Date(value).toISOString().slice(0, 10)
}

function formatDurationSeconds(durationMs?: number) {
  return ((durationMs || 0) / 1000).toFixed(2)
}

function knowledgeBaseName(knowledgeBaseId?: string) {
  if (!knowledgeBaseId) return '-'
  return knowledgeBases.value.find((item) => item.id === knowledgeBaseId)?.name || shortId(knowledgeBaseId)
}

function statusType(status?: string) {
  if (status === 'READY' || status === 'ACTIVE' || status === 'SUCCESS' || status === 'UP') {
    return 'success'
  }
  if (status === 'FAILED' || status === 'ERROR') return 'danger'
  if (status === 'PROCESSING') return 'warning'
  return 'info'
}

function selectKnowledgeBase(id: string) {
  const changed = selectedKnowledgeBaseId.value !== id
  selectedKnowledgeBaseId.value = id
  logFilter.knowledgeBaseId = id
  if (changed) {
    selectedConversationId.value = ''
    chatHistory.value = []
    chatResult.value = null
  }
  void refreshDocuments()
  void refreshConversations()
  void refreshLogs()
}

async function refreshHealth() {
  try {
    const health = await api.health()
    healthStatus.value = health.status
  } catch {
    healthStatus.value = 'DOWN'
  }
}

async function refreshKnowledgeBases() {
  knowledgeBases.value = await api.listKnowledgeBases()
  if (!selectedKnowledgeBaseId.value && knowledgeBases.value.length > 0) {
    selectedKnowledgeBaseId.value = knowledgeBases.value[0].id
    logFilter.knowledgeBaseId = selectedKnowledgeBaseId.value
  }
}

async function refreshDocuments() {
  if (!selectedKnowledgeBaseId.value) {
    documents.value = []
    return
  }
  documents.value = await api.listDocuments(selectedKnowledgeBaseId.value)
}

async function refreshConversations() {
  if (!selectedKnowledgeBaseId.value) {
    conversations.value = []
    selectedConversationId.value = ''
    return
  }
  conversations.value = await api.listConversations(selectedKnowledgeBaseId.value)
  const selectedStillValid = conversations.value.some((item) => item.id === selectedConversationId.value)
  if (!selectedStillValid) {
    selectedConversationId.value = ''
  }
  if (!selectedConversationId.value && conversations.value.length > 0) {
    selectedConversationId.value = conversations.value[0].id
  }
}

async function refreshLogs() {
  logs.value = await api.listInvocationLogs({
    knowledgeBaseId: logFilter.knowledgeBaseId || selectedKnowledgeBaseId.value || undefined,
    dateFrom: logFilter.dateFrom || undefined,
    dateTo: logFilter.dateTo || undefined,
  })
}

async function refreshAll() {
  loading.value = true
  try {
    await refreshHealth()
    await refreshKnowledgeBases()
    await refreshDocuments()
    await refreshConversations()
    await refreshLogs()
  } finally {
    loading.value = false
  }
}

async function login() {
  loading.value = true
  try {
    const result = await api.login({ username: authForm.username, password: authForm.password })
    setToken(result.token)
    currentUser.value = result.user
    ElMessage.success('登录成功')
    await refreshAll()
  } catch (error) {
    ElMessage.error((error as Error).message)
  } finally {
    loading.value = false
  }
}

async function register() {
  loading.value = true
  try {
    await api.register({
      username: authForm.username,
      password: authForm.password,
      email: authForm.email || undefined,
    })
    ElMessage.success('注册成功，已为你登录')
    await login()
  } catch (error) {
    ElMessage.error((error as Error).message)
    loading.value = false
  }
}

async function restoreSession() {
  booting.value = true
  try {
    await refreshHealth()
    if (getToken()) {
      currentUser.value = await api.me()
      await refreshAll()
    }
  } catch {
    clearToken()
    currentUser.value = null
  } finally {
    booting.value = false
  }
}

function logout() {
  clearToken()
  currentUser.value = null
  knowledgeBases.value = []
  documents.value = []
  logs.value = []
  chatHistory.value = []
  chatResult.value = null
}

async function createKnowledgeBase() {
  loading.value = true
  try {
    const kb = await api.createKnowledgeBase({
      name: knowledgeForm.name,
      description: knowledgeForm.description,
    })
    ElMessage.success('知识库已创建')
    await refreshKnowledgeBases()
    selectKnowledgeBase(kb.id)
  } catch (error) {
    ElMessage.error((error as Error).message)
  } finally {
    loading.value = false
  }
}

async function uploadDocument(options: UploadRequestOptions) {
  if (!selectedKnowledgeBaseId.value) {
    ElMessage.warning('请先选择知识库')
    return
  }
  loading.value = true
  try {
    await api.uploadDocument(selectedKnowledgeBaseId.value, options.file as File)
    ElMessage.success('文档已上传并完成索引')
    await refreshDocuments()
  } catch (error) {
    ElMessage.error((error as Error).message)
  } finally {
    loading.value = false
  }
}

async function downloadDocument(documentId: string, filename: string) {
  if (!selectedKnowledgeBaseId.value) {
    ElMessage.warning('请先选择知识库')
    return
  }
  try {
    const blob = await api.downloadDocument(selectedKnowledgeBaseId.value, documentId)
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename || '知识库文档'
    link.style.display = 'none'
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.setTimeout(() => URL.revokeObjectURL(url), 60_000)
    ElMessage.success('已开始下载原文件')
  } catch (error) {
    ElMessage.error((error as Error).message)
  }
}

async function ensureConversation() {
  if (!chatForm.useConversation || !selectedKnowledgeBaseId.value) return undefined
  const selectedConversation = conversations.value.find((item) => item.id === selectedConversationId.value)
  if (selectedConversation?.knowledgeBaseId === selectedKnowledgeBaseId.value) {
    return selectedConversation.id
  }
  selectedConversationId.value = ''
  const conversation = await api.createConversation({
    knowledgeBaseId: selectedKnowledgeBaseId.value,
    title: selectedKnowledgeBase.value?.name ? `${selectedKnowledgeBase.value.name}咨询` : '知识库咨询',
  })
  selectedConversationId.value = conversation.id
  await refreshConversations()
  return conversation.id
}

async function askQuestion() {
  if (!selectedKnowledgeBaseId.value) {
    ElMessage.warning('请先选择知识库')
    return
  }
  if (!chatForm.question.trim()) {
    ElMessage.warning('请输入问题')
    return
  }
  loading.value = true
  try {
    const question = chatForm.question.trim()
    chatHistory.value.push({ role: 'user', content: question })
    const conversationId = await ensureConversation()
    const result = await api.chat({
      knowledgeBaseId: selectedKnowledgeBaseId.value,
      question,
      topK: chatForm.topK,
      conversationId,
    })
    chatResult.value = result
    chatHistory.value.push({
      role: 'assistant',
      content: result.answer,
      references: result.references,
    })
    await refreshLogs()
  } catch (error) {
    ElMessage.error((error as Error).message)
  } finally {
    loading.value = false
  }
}

async function loadDemoDefaults() {
  knowledgeForm.name = '公司制度知识库'
  knowledgeForm.description = '用于回答员工手册、报销制度、请假制度等问题'
  chatForm.question = '员工报销发票有什么要求？'
}

onMounted(() => {
  void restoreSession()
})
</script>

<template>
  <div v-loading="booting" class="app-shell">
    <section v-if="!currentUser" class="auth-page">
      <div class="auth-panel">
        <div class="brand-block">
          <div class="brand-mark">KB</div>
          <div>
            <h1>AI 知识库商业 Demo</h1>
            <p>Docker Compose 一键启动的知识库问答控制台</p>
          </div>
        </div>

        <el-tabs v-model="authMode" stretch>
          <el-tab-pane label="登录" name="login" />
          <el-tab-pane label="注册" name="register" />
        </el-tabs>

        <el-form label-position="top" class="auth-form" @submit.prevent>
          <el-form-item label="用户名">
            <el-input v-model="authForm.username" size="large" :prefix-icon="User" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="authForm.password"
              size="large"
              type="password"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>
          <el-form-item v-if="authMode === 'register'" label="邮箱">
            <el-input v-model="authForm.email" size="large" :prefix-icon="Key" />
          </el-form-item>
          <el-button
            type="primary"
            size="large"
            class="full-button"
            :loading="loading"
            @click="authMode === 'login' ? login() : register()"
          >
            {{ authMode === 'login' ? '进入 Demo' : '创建账号并进入' }}
          </el-button>
        </el-form>
      </div>

      <div class="auth-side">
        <div class="system-card">
          <span>后端服务</span>
          <el-tag :type="statusType(healthStatus)" effect="dark">{{ healthStatus }}</el-tag>
        </div>
        <div class="auth-metrics">
          <div>
            <strong>PostgreSQL</strong>
            <span>业务数据 + pgvector</span>
          </div>
          <div>
            <strong>Redis</strong>
            <span>缓存与健康验证</span>
          </div>
          <div>
            <strong>Swagger</strong>
            <span>中文 API 文档</span>
          </div>
        </div>
      </div>
    </section>

    <template v-else>
      <header class="topbar">
        <div class="topbar-title">
          <div class="brand-mark compact">KB</div>
          <div>
            <h1>AI 知识库商业 Demo</h1>
            <span>Docker 本地环境</span>
          </div>
        </div>
        <div class="topbar-actions">
          <el-tag :type="statusType(healthStatus)" effect="plain">后端 {{ healthStatus }}</el-tag>
          <el-button :icon="Link" text tag="a" href="http://localhost:18080/swagger-ui.html" target="_blank">
            Swagger
          </el-button>
          <el-button :icon="Refresh" @click="refreshAll">刷新</el-button>
          <el-button :icon="SwitchButton" @click="logout">退出</el-button>
        </div>
      </header>

      <main class="workspace">
        <aside class="sidebar">
          <div class="user-box">
            <el-avatar :icon="User" />
            <div>
              <strong>{{ currentUser.username }}</strong>
              <span>{{ currentUser.email || currentUser.status }}</span>
            </div>
          </div>

          <el-menu v-model:default-active="activeWorkspace" class="nav-menu">
            <el-menu-item index="knowledge" @click="activeWorkspace = 'knowledge'">
              <el-icon><Collection /></el-icon>
              <span>知识库管理</span>
            </el-menu-item>
            <el-menu-item index="chat" @click="activeWorkspace = 'chat'">
              <el-icon><ChatDotRound /></el-icon>
              <span>知识库问答</span>
            </el-menu-item>
            <el-menu-item index="logs" @click="activeWorkspace = 'logs'">
              <el-icon><DataAnalysis /></el-icon>
              <span>调用记录</span>
            </el-menu-item>
          </el-menu>

          <div class="summary-grid">
            <div>
              <span>知识库</span>
              <strong>{{ knowledgeBases.length }}</strong>
            </div>
            <div>
              <span>已就绪文档</span>
              <strong>{{ readyDocumentCount }}</strong>
            </div>
            <div>
              <span>调用记录</span>
              <strong>{{ logs.length }}</strong>
            </div>
          </div>
        </aside>

        <section class="content" v-loading="loading">
          <div v-if="activeWorkspace === 'knowledge'" class="content-grid two-columns">
            <section class="panel">
              <div class="panel-header">
                <div>
                  <h2>知识库</h2>
                  <span>{{ knowledgeBases.length }} 个知识库</span>
                </div>
                <el-button :icon="Refresh" @click="refreshKnowledgeBases">刷新</el-button>
              </div>

              <el-empty v-if="knowledgeBases.length === 0" description="暂无知识库" />
              <div v-else class="kb-list">
                <button
                  v-for="item in knowledgeBases"
                  :key="item.id"
                  class="kb-item"
                  :class="{ active: item.id === selectedKnowledgeBaseId }"
                  @click="selectKnowledgeBase(item.id)"
                >
                  <span>
                    <strong>{{ item.name }}</strong>
                    <small>{{ item.description || '未填写描述' }}</small>
                  </span>
                  <el-tag :type="statusType(item.status)" size="small">{{ item.status }}</el-tag>
                </button>
              </div>
            </section>

            <section class="panel">
              <div class="panel-header">
                <div>
                  <h2>创建知识库</h2>
                  <span>{{ selectedKnowledgeBase?.name || '未选择知识库' }}</span>
                </div>
                <el-button text @click="loadDemoDefaults">Demo 数据</el-button>
              </div>

              <el-form label-position="top">
                <el-form-item label="知识库名称">
                  <el-input v-model="knowledgeForm.name" />
                </el-form-item>
                <el-form-item label="知识库描述">
                  <el-input v-model="knowledgeForm.description" type="textarea" :rows="3" />
                </el-form-item>
                <el-button type="primary" :icon="DocumentAdd" @click="createKnowledgeBase">
                  创建知识库
                </el-button>
              </el-form>
            </section>

            <section class="panel wide-panel">
              <div class="panel-header">
                <div>
                  <h2>文档</h2>
                  <span>{{ selectedDocuments.length }} 个文档</span>
                </div>
                <el-upload
                  :show-file-list="false"
                  :http-request="uploadDocument"
                  accept=".md,.txt,.pdf"
                >
                  <el-button type="primary" :icon="UploadFilled" :disabled="!selectedKnowledgeBaseId">
                    上传文档
                  </el-button>
                </el-upload>
              </div>

              <el-table :data="selectedDocuments" empty-text="暂无文档" stripe>
                <el-table-column prop="originalFilename" label="文件名" min-width="220" />
                <el-table-column label="状态" width="120">
                  <template #default="{ row }">
                    <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="chunkCount" label="切片" width="90" />
                <el-table-column label="大小" width="110">
                  <template #default="{ row }">{{ Math.ceil(row.fileSize / 1024) }} KB</template>
                </el-table-column>
                <el-table-column label="更新时间" min-width="180">
                  <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
                </el-table-column>
              </el-table>
            </section>
          </div>

          <div v-else-if="activeWorkspace === 'chat'" class="content-grid chat-layout">
            <section class="panel chat-panel">
              <div class="panel-header">
                <div>
                  <h2>知识库问答</h2>
                  <span>{{ selectedKnowledgeBase?.name || '请选择知识库' }}</span>
                </div>
                <el-select v-model="selectedKnowledgeBaseId" placeholder="选择知识库" @change="selectKnowledgeBase">
                  <el-option
                    v-for="item in knowledgeBases"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </div>

              <div class="chat-window">
                <el-empty v-if="chatHistory.length === 0" description="暂无问答记录" />
                <div
                  v-for="(item, index) in chatHistory"
                  :key="index"
                  class="message-row"
                  :class="item.role"
                >
                  <div class="message-bubble">
                    <strong>{{ item.role === 'user' ? '用户' : 'AI' }}</strong>
                    <p>{{ item.content }}</p>
                    <div v-if="item.references?.length" class="reference-list">
                      <div v-for="ref in item.references" :key="`${ref.documentId}-${ref.chunkIndex}`">
                        <span>{{ ref.fileName }}</span>
                        <el-button
                          size="small"
                          text
                          :icon="Download"
                          @click="downloadDocument(ref.documentId, ref.fileName)"
                        >
                          下载原文件
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="ask-box">
                <el-input
                  v-model="chatForm.question"
                  type="textarea"
                  :rows="3"
                  resize="none"
                  placeholder="输入问题"
                  @keydown.enter.exact.prevent="askQuestion"
                  @keydown.shift.enter.stop
                />
                <div class="ask-actions">
                  <el-checkbox v-model="chatForm.useConversation">写入会话</el-checkbox>
                  <el-input-number v-model="chatForm.topK" :min="1" :max="20" controls-position="right" />
                  <el-button type="primary" :icon="Search" @click="askQuestion">提问</el-button>
                </div>
              </div>
            </section>

            <section class="panel">
              <div class="panel-header">
                <div>
                  <h2>会话</h2>
                  <span>{{ conversations.length }} 条</span>
                </div>
              </div>
              <el-select v-model="selectedConversationId" placeholder="自动创建会话" clearable class="full-select">
                <el-option
                  v-for="item in conversations"
                  :key="item.id"
                  :label="item.title || shortId(item.id)"
                  :value="item.id"
                />
              </el-select>

              <div v-if="chatResult" class="result-meta">
                <div>
                  <span>调用记录</span>
                  <strong>{{ shortId(chatResult.invocationLogId) }}</strong>
                </div>
                <div>
                  <span>引用片段</span>
                  <strong>{{ chatResult.references?.length || 0 }}</strong>
                </div>
              </div>

              <div class="reference-detail">
                <h3>引用来源</h3>
                <el-empty v-if="!chatResult?.references?.length" description="暂无引用" />
                <div v-for="ref in chatResult?.references || []" :key="`${ref.documentId}-${ref.chunkIndex}`">
                  <div class="reference-title">
                    <strong>{{ ref.fileName }}</strong>
                    <el-button
                      size="small"
                      :icon="Download"
                      @click="downloadDocument(ref.documentId, ref.fileName)"
                    >
                      下载原文件
                    </el-button>
                  </div>
                  <p>{{ ref.content }}</p>
                </div>
              </div>
            </section>
          </div>

          <div v-else class="content-grid">
            <section class="panel wide-panel">
              <div class="panel-header">
                <div>
                  <h2>调用记录</h2>
                  <span>{{ logs.length }} 条记录</span>
                </div>
                <div class="filter-row">
                  <el-select v-model="logFilter.knowledgeBaseId" placeholder="全部知识库" clearable>
                    <el-option
                      v-for="item in knowledgeBases"
                      :key="item.id"
                      :label="item.name"
                      :value="item.id"
                    />
                  </el-select>
                  <el-date-picker v-model="logFilter.dateFrom" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" />
                  <el-date-picker v-model="logFilter.dateTo" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" />
                  <el-button :icon="Search" @click="refreshLogs">查询</el-button>
                </div>
              </div>

              <el-table :data="latestLogs" empty-text="暂无调用记录" stripe>
                <el-table-column label="状态" width="110">
                  <template #default="{ row }">
                    <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="modelName" label="模型" min-width="190" />
                <el-table-column label="Token" width="120">
                  <template #default="{ row }">{{ row.totalTokens }}</template>
                </el-table-column>
                <el-table-column label="耗时" width="120">
                  <template #default="{ row }">{{ formatDurationSeconds(row.durationMs) }} 秒</template>
                </el-table-column>
                <el-table-column label="知识库" min-width="160">
                  <template #default="{ row }">{{ knowledgeBaseName(row.knowledgeBaseId) }}</template>
                </el-table-column>
                <el-table-column label="时间" width="140">
                  <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
                </el-table-column>
              </el-table>
            </section>

            <section class="metrics-strip">
              <div>
                <el-icon><Finished /></el-icon>
                <span>成功调用</span>
                <strong>{{ logs.filter((item) => item.status === 'SUCCESS').length }}</strong>
              </div>
              <div>
                <el-icon><DataAnalysis /></el-icon>
                <span>总 Token</span>
                <strong>{{ logs.reduce((sum, item) => sum + item.totalTokens, 0) }}</strong>
              </div>
              <div>
                <el-icon><CircleCheck /></el-icon>
                <span>平均耗时</span>
                <strong>{{ averageDurationSeconds }} 秒</strong>
              </div>
            </section>
          </div>
        </section>
      </main>
    </template>
  </div>
</template>
