import type { AppUser, UserProfile } from '../types'
import * as userProfileService from './userProfileService'

const profileCache = new Map<string, UserProfile>()

export function cacheProfile(profile: UserProfile) {
  profileCache.set(profile.uid, profile)
}

export function profileForUid(uid: string): UserProfile | undefined {
  return profileCache.get(uid)
}

export function handleForUid(uid: string): string | undefined {
  return profileCache.get(uid)?.handle
}

export async function fetchByUid(uid: string): Promise<UserProfile | null> {
  const cached = profileCache.get(uid)
  if (cached) return cached
  const profile = await userProfileService.fetchByUid(uid)
  if (profile) cacheProfile(profile)
  return profile
}

export async function fetchByHandle(handle: string): Promise<UserProfile | null> {
  const profile = await userProfileService.fetchByHandle(handle)
  if (profile) cacheProfile(profile)
  return profile
}

export async function ensureForUser(user: AppUser): Promise<UserProfile> {
  const profile = await userProfileService.ensureProfile({
    uid: user.uid,
    displayName: user.displayName,
    photoUrl: user.photoUrl
  })
  cacheProfile(profile)
  return profile
}
