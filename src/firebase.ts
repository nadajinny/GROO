import { initializeApp } from 'firebase/app'
import { getAnalytics, isSupported, type Analytics } from 'firebase/analytics'
import { getFirestore } from 'firebase/firestore'
import { getStorage } from 'firebase/storage'

const firebaseConfig = {
  apiKey: 'AIzaSyAK3Of_C2lmiKVm7DtKuBjklkv2PXRoEzk',
  authDomain: 'lab-tool-a1e1e.firebaseapp.com',
  databaseURL: 'https://lab-tool-a1e1e-default-rtdb.firebaseio.com',
  projectId: 'lab-tool-a1e1e',
  storageBucket: 'lab-tool-a1e1e.firebasestorage.app',
  messagingSenderId: '585409538114',
  appId: '1:585409538114:web:60d98f76dd3faa1f9025db',
  measurementId: 'G-VD9GYK96YD'
}

const app = initializeApp(firebaseConfig)

let analyticsPromise: Promise<Analytics | undefined> | undefined

if (typeof window !== 'undefined') {
  analyticsPromise = isSupported()
    .then((supported) => {
      if (!supported) return undefined
      return getAnalytics(app)
    })
    .catch(() => undefined)
}

const firestore = getFirestore(app)
const storage = getStorage(app)

export { app, firestore, storage, analyticsPromise }
