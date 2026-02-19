'use client';

import { Send } from 'lucide-react';
import { useEffect, useState } from 'react';

interface NotificationItem {
  id: number;
  title: string;
  message: string;
  target: string;
  sentAt: string;
  recipientCount: number;
}

const TARGET_MAP: Record<string, string> = { all: '전체 사용자', active: '활성 사용자만', specific_group: '특정 그룹' };

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [form, setForm] = useState({ title: '', message: '', target: 'all' });

  const fetchNotifications = () => {
    fetch('/api/v1/admin/notifications').then((r) => r.json()).then((d) => {
      if (d.success) setNotifications(d.data);
    });
  };

  useEffect(() => { fetchNotifications(); }, []);

  const handleSend = async () => {
    try {
      const res = await fetch('/api/v1/admin/notifications', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      });
      const data = await res.json();
      if (data.success) {
        setForm({ title: '', message: '', target: 'all' });
        fetchNotifications();
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
            <label className="block text-sm font-medium text-gray-700 mb-2">수신 대상</label>
            <select value={form.target} onChange={(e) => setForm({ ...form, target: e.target.value })}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
              <option value="all">전체 사용자</option>
              <option value="active">활성 사용자만</option>
              <option value="specific_group">특정 그룹</option>
            </select>
          </div>
          <div className="flex gap-3">
            <button onClick={handleSend}
              className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
              <Send className="w-4 h-4" />
              즉시 전송
            </button>
            <button className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors">
              예약 전송
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
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">전송일시</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">수신자 수</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {notifications.map((n) => (
              <tr key={n.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 text-sm font-medium text-gray-900">{n.title}</td>
                <td className="px-6 py-4 text-sm text-gray-600">{n.message}</td>
                <td className="px-6 py-4 text-sm text-gray-600">{new Date(n.sentAt).toLocaleString()}</td>
                <td className="px-6 py-4 text-sm text-gray-600">{n.recipientCount.toLocaleString()}명</td>
              </tr>
            ))}
            {notifications.length === 0 && (
              <tr><td colSpan={4} className="px-6 py-8 text-center text-sm text-gray-500">전송 내역이 없습니다</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
