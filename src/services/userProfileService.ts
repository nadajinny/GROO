import {
  collection,
  doc,
  getDoc,
  getDocs,
  limit,
  query,
  serverTimestamp,
  setDoc,
  where
} from 'firebase/firestore'

import { firestore } from '../firebase'
import type { UserProfile } from '../types'

const profilesRef = collection(firestore, 'userProfiles')

export async function ensureProfile(params: {
  uid: string
  displayName: string | null
  photoUrl: string | null
}): Promise<UserProfile> {
  const docRef = doc(profilesRef, params.uid)
  const snapshot = await getDoc(docRef)
  const sanitizedName = sanitizeUserId(params.displayName)

  if (snapshot.exists()) {
    const current = snapshot.data()
    if (!current) {
      return await createProfile({
        uid: params.uid,
        userId: sanitizedName,
        displayName: params.displayName ?? '사용자',
        photoUrl: params.photoUrl
      })
    }
    const existing = mapProfile(snapshot.id, current)
    const needsNewTag =
      !current.handle ||
      typeof current.tag !== 'string' ||
      current.tag.length !== 5 ||
      existing.userId !== sanitizedName
    const needsDisplayUpdate =
      (params.displayName ?? existing.displayName) !== existing.displayName
    const needsPhotoUpdate = params.photoUrl && params.photoUrl !== existing.photoUrl

    if (needsNewTag || needsDisplayUpdate || needsPhotoUpdate) {
      const tag = needsNewTag ? await generateUniqueTag(sanitizedName) : existing.tag
      const updated: UserProfile = {
        uid: params.uid,
        userId: sanitizedName,
        tag,
        displayName: params.displayName ?? existing.displayName,
        photoUrl: params.photoUrl ?? existing.photoUrl ?? null,
        handle: `${sanitizedName}#${tag}`
      }
      await setDoc(docRef, serializeProfile(updated), { merge: true })
      return updated
    }
    return existing
  }

  return await createProfile({
    uid: params.uid,
    userId: sanitizedName,
    displayName: params.displayName ?? '사용자',
    photoUrl: params.photoUrl
  })
}

export async function fetchByUid(uid: string): Promise<UserProfile | null> {
  const snapshot = await getDoc(doc(profilesRef, uid))
  if (!snapshot.exists()) return null
  return mapProfile(snapshot.id, snapshot.data()!)
}

export async function fetchByHandle(handle: string): Promise<UserProfile | null> {
  const normalized = normalizeHandle(handle)
  if (!normalized) return null
  const q = query(
    profilesRef,
    where('userId', '==', normalized.userId),
    where('tag', '==', normalized.tag),
    limit(1)
  )
  const snapshot = await getDocs(q)
  if (snapshot.empty) return null
  const [firstDoc] = snapshot.docs
  if (!firstDoc) return null
  return mapProfile(firstDoc.id, firstDoc.data())
}

export async function updateNickname(params: {
  uid: string
  newNickname: string
  photoUrl?: string | null
}): Promise<UserProfile> {
  const sanitized = sanitizeUserId(params.newNickname)
  const tag = await generateUniqueTag(sanitized)
  const docRef = doc(profilesRef, params.uid)
  const profile: UserProfile = {
    uid: params.uid,
    userId: sanitized,
    tag,
    displayName: params.newNickname.trim() || '사용자',
    photoUrl: params.photoUrl ?? null,
    handle: `${sanitized}#${tag}`
  }
  await setDoc(docRef, serializeProfile(profile), { merge: true })
  return profile
}

async function createProfile(params: {
  uid: string
  userId: string
  displayName: string
  photoUrl: string | null
}): Promise<UserProfile> {
  const tag = await generateUniqueTag(params.userId)
  const profile: UserProfile = {
    uid: params.uid,
    userId: params.userId,
    tag,
    displayName: params.displayName,
    photoUrl: params.photoUrl,
    handle: `${params.userId}#${tag}`
  }
  await setDoc(doc(profilesRef, params.uid), serializeProfile(profile))
  return profile
}

function sanitizeUserId(displayName: string | null): string {
  const base = (displayName ?? 'user').trim().replace(/\s+/g, '').toLowerCase()
  if (!base) return 'user'
  return base.slice(0, 20)
}

async function generateUniqueTag(userId: string, retries = 10): Promise<string> {
  for (let i = 0; i < retries; i += 1) {
    const tag = String(Math.floor(10000 + Math.random() * 90000))
    const exists = await getDocs(
      query(
        profilesRef,
        where('userId', '==', userId),
        where('tag', '==', tag),
        limit(1)
      )
    )
    if (exists.empty) return tag
  }
  return String(Math.floor(10000 + Math.random() * 90000))
}

function normalizeHandle(handle: string): { userId: string; tag: string } | null {
  const [idPart, tagPart] = handle.trim().split('#')
  if (!idPart || !tagPart) return null
  const userId = idPart.trim().toLowerCase()
  const tag = tagPart.trim()
  if (!userId || tag.length !== 5) return null
  return { userId, tag }
}

function mapProfile(id: string, data: Record<string, any> | undefined): UserProfile {
  const record: Record<string, any> = data ?? {}
  const rawUser = record.userId ?? 'user'
  const rawTag = record.tag ?? '00000'
  const userId =
    typeof rawUser === 'string'
      ? rawUser.trim().toLowerCase()
      : String(rawUser)
  const tag =
    typeof rawTag === 'string'
      ? rawTag.padStart(5, '0')
      : String(rawTag).padStart(5, '0')
  return {
    uid: record.uid ?? id,
    userId: userId || 'user',
    tag,
    displayName: record.displayName ?? '사용자',
    photoUrl: record.photoUrl ?? null,
    handle: record.handle ?? `${userId}#${tag}`
  }
}

function serializeProfile(profile: UserProfile) {
  return {
    uid: profile.uid,
    userId: profile.userId,
    tag: profile.tag,
    handle: profile.handle,
    displayName: profile.displayName,
    photoUrl: profile.photoUrl ?? null,
    updatedAt: serverTimestamp()
  }
}
