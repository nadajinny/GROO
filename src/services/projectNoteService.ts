import {
  addDoc,
  collection,
  deleteDoc,
  doc,
  getDocs,
  orderBy,
  query,
  serverTimestamp
} from 'firebase/firestore'

import { firestore } from '../firebase'
import type { ProjectNote } from '../types'

function notesCollection(projectId: string) {
  return collection(firestore, 'projects', projectId, 'notes')
}

export async function getProjectNotes(projectId: string): Promise<ProjectNote[]> {
  const snapshot = await getDocs(query(notesCollection(projectId), orderBy('createdAt', 'desc')))
  return snapshot.docs.map((doc) => {
    const data = doc.data()
    return {
      id: doc.id,
      projectId,
      title: data.title ?? '제목 없음',
      content: data.content ?? '',
      createdBy: data.createdBy ?? 'unknown',
      createdAt: data.createdAt ? data.createdAt.toDate() : null,
      updatedAt: data.updatedAt ? data.updatedAt.toDate() : null
    }
  })
}

export async function addProjectNote(params: {
  projectId: string
  title: string
  content: string
  userId: string
}) {
  await addDoc(notesCollection(params.projectId), {
    projectId: params.projectId,
    title: params.title,
    content: params.content,
    createdBy: params.userId,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  })
}

export async function deleteProjectNote(params: { projectId: string; noteId: string }) {
  await deleteDoc(doc(firestore, 'projects', params.projectId, 'notes', params.noteId))
}
