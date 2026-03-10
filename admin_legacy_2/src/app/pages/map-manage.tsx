import { Plus, MapPin } from "lucide-react";

export function MapManage() {
  const places = [
    { id: 1, name: "메인 게이트", category: "입구", lat: 37.5665, lng: 126.978 },
    { id: 2, name: "푸드 코트", category: "식음료", lat: 37.5665, lng: 126.979 },
    { id: 3, name: "공연장", category: "시설", lat: 37.5666, lng: 126.978 },
    { id: 4, name: "포토존 1", category: "포토존", lat: 37.5664, lng: 126.978 },
  ];

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-semibold text-gray-900">지도 / 장소 관리</h2>
          <p className="text-sm text-gray-500 mt-1">프로그램 장소를 등록하고 관리합니다</p>
        </div>
        <button className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
          <Plus className="w-4 h-4" />
          장소 등록
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 장소 리스트 */}
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">등록된 장소</h3>
          <div className="space-y-3">
            {places.map((place) => (
              <div
                key={place.id}
                className="flex items-center gap-3 p-4 rounded-lg border border-gray-200 hover:border-blue-300 transition-colors"
              >
                <div className="p-2 bg-blue-100 rounded-lg">
                  <MapPin className="w-5 h-5 text-blue-600" />
                </div>
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-900">{place.name}</p>
                  <p className="text-xs text-gray-500">{place.category}</p>
                </div>
                <button className="text-sm text-blue-600 hover:underline">
                  상세
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* 지도 미리보기 */}
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">지도 미리보기</h3>
          <div className="aspect-square bg-gray-100 rounded-lg flex items-center justify-center">
            <div className="text-center text-gray-500">
              <MapPin className="w-12 h-12 mx-auto mb-2 text-gray-400" />
              <p className="text-sm">지도 영역</p>
              <p className="text-xs mt-1">실제 앱에서는 지도가 표시됩니다</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}