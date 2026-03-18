'use client';

import { Clock, Send } from 'lucide-react';
import { useEffect, useState } from 'react';

interface NotificationItem {
  id: number;
  title: string;
  message: string;
  target: string;
  status: string;
  scheduledAt: string | null;
  sentAt: string;
  recipientCount: number;
}

const TARGET_MAP: Record<string, string> = { all: '전체 사용자', active: '활성 사용자만', specific_group: '특정 그룹' };

const STATUS_MAP: Record<string, { label: string; color: string }> = {
  sent: { label: '전송 완료', color: 'bg-green-100 text-green-700' },
  scheduled: { label: '예약', color: 'bg-blue-100 text-blue-700' },
  failed: { label: '실패', color: 'bg-red-100 text-red-700' },
};

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [sendMode, setSendMode] = useState<'immediate' | 'scheduled'>('immediate');
  const [form, setForm] = useState({ title: '', message: '', target: 'all', scheduledAt: '' });

  const fetchNotifications = () => {
    fetch('/api/v1/admin/notifications').then((r) => r.json()).then((d) => {
      if (d.success) setNotifications(d.data);
    });
  };

  useEffect(() => { fetchNotifications(); }, []);

  const handleSend = async () => {
    if (sendMode === 'scheduled' && !form.scheduledAt) {
      alert('예약 전송 시간을 선택해주세요');
      return;
    }

    try {
      const payload: Record<string, string> = {
        title: form.title,
        message: form.message,
        target: form.target,
      };
      if (sendMode === 'scheduled' && form.scheduledAt) {
        payload.scheduledAt = new Date(form.scheduledAt).toISOString();
      }

      const res = await fetch('/api/v1/admin/notifications', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      const data = await res.json();
      if (data.success) {
        setForm({ title: '', message: '', target: 'all', scheduledAt: '' });
        setSendMode('immediate');
        fetchNotifications();
        alert(data.message || '알림이 처리되었습니다');
      } else {
        alert(`알림 전송 실패: ${data.error?.message || '알 수 없는 오류'}`);
      }
    } catch {
      alert('알림 전송 실패: 서버와 통신할 수 없습니다');
    }
  };

  return (
    <div className="p-8">
      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-gray-900">알림 관리</h2>
        <p className="text-sm text-gray-500 mt-1">사용자에게 푸시 알림을 전송합니다</p>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">새 알림 전송</h3>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">알림 제목</label>
            <input type="text" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })}
              placeholder="알림 제목을 입력하세요"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">알림 내용</label>
            <textarea value={form.message} onChange={(e) => setForm({ ...form, message: e.target.value })}
              placeholder="알림 내용을 입력하세요" rows={4}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">전송 방식</label>
            <div className="flex gap-4">
              <label className="flex items-center gap-2 cursor-pointer">
                <input type="radio" name="sendMode" value="immediate"
                  checked={sendMode === 'immediate'}
                  onChange={() => setSendMode('immediate')}
                  className="w-4 h-4 text-blue-600" />
                <span className="text-sm text-gray-700">즉시 전송</span>
              </label>
              <label className="flex items-center gap-2 cursor-pointer">
                <input type="radio" name="sendMode" value="scheduled"
                  checked={sendMode === 'scheduled'}
                  onChange={() => setSendMode('scheduled')}
                  className="w-4 h-4 text-blue-600" />
                <span className="text-sm text-gray-700">예약 전송</span>
              </label>
            </div>
          </div>
          {sendMode === 'scheduled' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">예약 전송 시간</label>
              <input type="datetime-local" value={form.scheduledAt}
                onChange={(e) => setForm({ ...form, scheduledAt: e.target.value })}
                min={new Date().toISOString().slice(0, 16)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
            </div>
          )}
          <div>
            <button onClick={handleSend}
              className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
              {sendMode === 'immediate' ? (
                <>
                  <Send className="w-4 h-4" />
                  즉시 전송
                </>
              ) : (
                <>
                  <Clock className="w-4 h-4" />
                  예약 전송
                </>
              )}
            </button>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-medium text-gray-900">전송 내역</h3>
        </div>
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">제목</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">내용</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">전송/예약 일시</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">수신자 수</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {notifications.map((n) => {
              const statusInfo = STATUS_MAP[n.status] || { label: n.status, color: 'bg-gray-100 text-gray-700' };
              return (
                <tr key={n.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 text-sm font-medium text-gray-900">{n.title}</td>
                  <td className="px-6 py-4 text-sm text-gray-600">{n.message}</td>
                  <td className="px-6 py-4 text-sm">
                    <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${statusInfo.color}`}>
                      {statusInfo.label}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-600">
                    {n.status === 'scheduled' && n.scheduledAt
                      ? new Date(n.scheduledAt).toLocaleString()
                      : new Date(n.sentAt).toLocaleString()}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-600">{n.recipientCount.toLocaleString()}명</td>
                </tr>
              );
            })}
            {notifications.length === 0 && (
              <tr><td colSpan={5} className="px-6 py-8 text-center text-sm text-gray-500">전송 내역이 없습니다</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
