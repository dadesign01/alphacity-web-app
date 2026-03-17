import admin from 'firebase-admin';
import { readFileSync, existsSync } from 'fs';
import { join } from 'path';

function initializeFirebase() {
  if (admin.apps.length > 0) return admin;

  const serviceAccountPath = join(process.cwd(), 'firebase-service-account.json');

  if (existsSync(serviceAccountPath)) {
    const serviceAccount = JSON.parse(readFileSync(serviceAccountPath, 'utf-8'));
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount),
    });
  } else {
    console.warn('Firebase service account not found. Push notifications disabled.');
    return null;
  }

  return admin;
}

const firebaseAdmin = initializeFirebase();

export async function sendPushNotification(
  tokens: string[],
  title: string,
  body: string,
) {
  if (!firebaseAdmin || tokens.length === 0) return { successCount: 0, failureCount: 0 };

  const message: admin.messaging.MulticastMessage = {
    tokens,
    notification: { title, body },
    android: {
      priority: 'high',
      notification: { channelId: 'default', sound: 'default' },
    },
    apns: {
      payload: { aps: { sound: 'default', badge: 1 } },
    },
  };

  try {
    const response = await firebaseAdmin.messaging().sendEachForMulticast(message);
    return {
      successCount: response.successCount,
      failureCount: response.failureCount,
    };
  } catch (error) {
    console.error('FCM send error:', error);
    return { successCount: 0, failureCount: tokens.length };
  }
}

export default firebaseAdmin;
