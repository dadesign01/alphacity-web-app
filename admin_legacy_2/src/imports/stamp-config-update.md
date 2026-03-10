스탬프 설정 화면 수정 프롬프트 (피그마용)

Modify the existing admin dashboard page titled "스탬프 설정 (Stamp Configuration)" inside the 프로그램 연결 설정 (Program Mapping) page.

Purpose:
Allow administrators to either create new stamps for the selected program or import stamps from an existing stamp library.

Keep the existing UI structure but enhance the stamp creation workflow.

Top Right Button

Replace the current button:

스탬프 추가

With a dropdown action button that includes two options:

새 스탬프 만들기 (Create New Stamp)

기존 스탬프 가져오기 (Import Existing Stamp)

Option 1: Create New Stamp

When selecting 새 스탬프 만들기, open a stamp creation card similar to the existing UI.

Fields:

Stamp name

Stamp description

Stamp color picker

Stamp icon upload

Stamp preview

Issuance condition dropdown

Issuance condition options:

미션 완료

장소 방문

이벤트 참여

Use a card layout for each stamp.

Each stamp card should include:

Stamp preview icon

Editable form fields

Delete icon

Collapse / expand option

Option 2: Import Existing Stamp

When selecting 기존 스탬프 가져오기, open a modal titled "스탬프 라이브러리 (Stamp Library)".

Modal content:

Table list of existing stamps including:

Stamp icon

Stamp name

Description

Created date

Allow:

Checkbox multi-select

Search field

Filter options

Buttons:

선택한 스탬프 추가 (Add Selected Stamps)

취소

Selected stamps should appear in the program stamp list as editable cards.

Imported Stamp Behavior

Even when a stamp is imported from the library, allow configuration for the current program:

Editable fields:

Issuance condition

Linked mission

Linked place

UI Style

Maintain the current admin UI style:

Card layout

White background

Blue primary button

Soft shadows

Rounded corners

Clean SaaS dashboard design