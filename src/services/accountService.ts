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

export interface AccountRecord {
  uid: string
  email: string
  passwordHash: string
  userId: string
}

const accountsCollection = collection(firestore, 'accounts')

export async function createAccount(record: AccountRecord) {
  await setDoc(doc(accountsCollection, record.uid), {
    email: normalizeEmail(record.email),
    passwordHash: record.passwordHash,
    userId: record.userId,
    createdAt: serverTimestamp()
  })
}

export async function findByEmail(email: string): Promise<AccountRecord | null> {
  const normalized = normalizeEmail(email)
  const q = query(accountsCollection, where('email', '==', normalized), limit(1))
  const snapshot = await getDocs(q)
  if (snapshot.empty) return null
  const docSnap = snapshot.docs[0]
  const data = docSnap.data()
  return {
    uid: docSnap.id,
    email: data.email ?? normalized,
    passwordHash: data.passwordHash ?? '',
    userId: data.userId ?? ''
  }
}

export async function findByUid(uid: string): Promise<AccountRecord | null> {
  const docSnap = await getDoc(doc(accountsCollection, uid))
  if (!docSnap.exists()) return null
  const data = docSnap.data()
  return {
    uid: docSnap.id,
    email: data.email ?? '',
    passwordHash: data.passwordHash ?? '',
    userId: data.userId ?? ''
  }
}

function normalizeEmail(email: string) {
  return email.trim().toLowerCase()
}
