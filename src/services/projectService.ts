import { apiRequest } from './apiClient'
import type { Project } from '../types'

interface BackendProject {
  id: number
  groupId: number
  name: string
  description?: string | null
  status: 'ACTIVE' | 'ARCHIVED'
  createdBy: number
  createdAt?: string | null
  updatedAt?: string | null
}

export async function getProjectsForGroup(groupId: string): Promise<Project[]> {
  const data = await apiRequest<BackendProject[]>(`/api/projects?groupId=${groupId}`)
  return data.map(mapProject)
}

export async function createProject(params: {
  groupId: string
  name: string
  description?: string
}): Promise<string> {
  const payload = {
    groupId: Number(params.groupId),
    name: params.name,
    description: params.description
  }
  const project = await apiRequest<BackendProject>('/api/projects', {
    method: 'POST',
    body: payload
  })
  return project.id.toString()
}

function mapProject(project: BackendProject): Project {
  return {
    id: project.id.toString(),
    groupId: project.groupId.toString(),
    name: project.name,
    description: project.description ?? '',
    status: project.status,
    createdBy: project.createdBy.toString(),
    createdAt: project.createdAt ? new Date(project.createdAt) : null
  }
}
