import {
  addDoc,
  collection,
  getDocs,
  orderBy,
  query,
  serverTimestamp,
  where
} from 'firebase/firestore'

import { firestore } from '../firebase'
import type { Project, ProjectStatus } from '../types'
import { projectStatusFromString } from '../types'

const projectsCollection = collection(firestore, 'projects')

export async function createProject(params: {
  groupId: string
  name: string
  description?: string
  currentUserId: string
}): Promise<string> {
  const trimmedName = params.name.trim()
  if (!trimmedName) {
    throw new Error('프로젝트 이름을 입력해주세요.')
  }
  const docRef = await addDoc(projectsCollection, {
    groupId: params.groupId,
    name: trimmedName,
    description: params.description?.trim() ?? '',
    status: 'ACTIVE' as ProjectStatus,
    createdBy: params.currentUserId,
    createdAt: serverTimestamp()
  })
  return docRef.id
}

export async function getProjectsForGroup(groupId: string): Promise<Project[]> {
  const q = query(
    projectsCollection,
    where('groupId', '==', groupId),
    orderBy('createdAt', 'desc')
  )
  const snapshot = await getDocs(q)
  return snapshot.docs.map((doc) => {
    const data = doc.data()
    return {
      id: doc.id,
      groupId: data.groupId ?? '',
      name: data.name ?? '이름 없음',
      description: data.description ?? '',
      status: projectStatusFromString(data.status),
      createdBy: data.createdBy ?? '',
      createdAt: data.createdAt ? data.createdAt.toDate() : null
    } as Project
  })
}
