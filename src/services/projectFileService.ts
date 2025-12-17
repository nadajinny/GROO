import {
  addDoc,
  collection,
  deleteDoc,
  doc,
  getDoc,
  getDocs,
  orderBy,
  query,
  serverTimestamp
} from 'firebase/firestore'
import {
  deleteObject,
  getDownloadURL,
  ref as storageRef,
  uploadBytes
} from 'firebase/storage'

import { firestore, storage } from '../firebase'
import type { ProjectFile } from '../types'

function filesCollection(projectId: string) {
  return collection(firestore, 'projects', projectId, 'files')
}

export async function getProjectFiles(projectId: string): Promise<ProjectFile[]> {
  const snapshot = await getDocs(query(filesCollection(projectId), orderBy('uploadedAt', 'desc')))
  return snapshot.docs.map((doc) => {
    const data = doc.data()
    return {
      id: doc.id,
      projectId,
      fileName: data.fileName ?? 'unknown',
      downloadUrl: data.downloadUrl ?? '',
      storagePath: data.storagePath ?? '',
      uploadedBy: data.uploadedBy ?? 'unknown',
      uploadedAt: data.uploadedAt ? data.uploadedAt.toDate() : null,
      size: Number(data.size ?? 0)
    }
  })
}

export async function uploadProjectFile(params: {
  projectId: string
  fileName: string
  data: ArrayBuffer | Uint8Array
  contentType: string
  userId: string
}): Promise<ProjectFile> {
  const timestamp = Date.now()
  const sanitized = params.fileName.replace(/[^A-Za-z0-9._-]/g, '_')
  const path = `projects/${params.projectId}/files/${timestamp}_${sanitized}`
  const ref = storageRef(storage, path)
  await uploadBytes(ref, params.data, {
    contentType: params.contentType,
    customMetadata: {
      uploadedBy: params.userId,
      projectId: params.projectId
    }
  })
  const downloadUrl = await getDownloadURL(ref)
  const docRef = await addDoc(filesCollection(params.projectId), {
    projectId: params.projectId,
    fileName: params.fileName,
    downloadUrl,
    storagePath: path,
    uploadedBy: params.userId,
    uploadedAt: serverTimestamp(),
    size: params.data instanceof ArrayBuffer ? params.data.byteLength : params.data.length
  })
  const savedDoc = await getDoc(docRef)
  return {
    id: savedDoc.id,
    projectId: params.projectId,
    fileName: params.fileName,
    downloadUrl,
    storagePath: path,
    uploadedBy: params.userId,
    uploadedAt: savedDoc.data()?.uploadedAt?.toDate?.() ?? new Date(),
    size: params.data instanceof ArrayBuffer ? params.data.byteLength : params.data.length
  }
}

export async function deleteProjectFile(params: { projectId: string; file: ProjectFile }) {
  await deleteDoc(doc(firestore, 'projects', params.projectId, 'files', params.file.id))
  if (params.file.storagePath) {
    try {
      await deleteObject(storageRef(storage, params.file.storagePath))
    } catch (error) {
      // ignore missing file
    }
  }
}
