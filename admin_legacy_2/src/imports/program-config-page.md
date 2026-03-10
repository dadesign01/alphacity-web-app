프로그램 연결 설정 페이지 (수정 버전)

Design an admin dashboard page titled "프로그램 연결 설정 (Program Configuration)".

Purpose:
Allow administrators to configure the core structure of a program by connecting places, missions, and stamps.

The program configuration should only include three main sections:

장소 연결 (Places)

미션 연결 (Missions)

스탬프 설정 (Stamps)

Layout:

Standard admin dashboard layout

Left sidebar navigation

Top header bar

Main content area with white cards

Clean SaaS admin UI style

Top Section (Program Selector):

Include a program selection area with:

Program dropdown selector

Program information card displaying:

Program name

Program period

Status (Active / Draft / Closed)

Category

Description

Below the program card create a tab navigation interface.

Tabs:

장소 연결 (Places)

미션 연결 (Missions)

스탬프 설정 (Stamps)

1. 장소 연결 (Places)

Purpose:
Assign locations to the selected program.

Interface:

Dual panel layout.

Left Panel:

List of registered places

Search input

Filter dropdown

Add button

Right Panel:

Places connected to this program

Remove button

Drag-and-drop ordering

Display place type (Entrance, Photo zone, Food court, etc.)

Behavior:

Each program can have different places

Administrators can add or remove places per program

2. 미션 연결 (Missions)

Purpose:
Attach mission types to the program and configure mission settings.

Mission types include:

위치 인증 (Location Check-in)

사진 인증 (Photo Mission)

퀴즈 (Quiz Mission)

체류 시간 (Stay Duration Mission)

Mission configuration rules:

Location Check-in:

Requires selected place

Optional radius setting

Photo Mission:

Requires image upload verification

Quiz Mission:

Requires question and answer fields

Stay Duration Mission:

Only available for Seminar type programs

Requires duration setting (ex: 5 minutes)

Interface:

Left Panel:

List of mission templates

Mission type icon

Add button

Right Panel:

Missions connected to the program

Each connected mission should allow configuration fields:

Mission type

Linked place

Verification method

Completion condition

Reward stamp

3. 스탬프 설정 (Stamps)

Purpose:
Define stamps used in the selected program.

Each program has its own stamp style and name.

Stamp settings include:

Stamp name

Stamp icon / image

Stamp description

Stamp color or theme

Issuance condition

Interface:

Stamp list inside a card layout.

Fields for each stamp:

Stamp name input

Stamp icon upload

Stamp preview

Stamp description

Stamp reward condition

Example conditions:

Mission completion

Place visit

Event participation

UI components:

Image upload

Stamp preview

Toggle switches

Card layout for each stamp

UI Style

Modern SaaS admin dashboard

Card-based layout

Soft shadows

Rounded UI elements

Blue primary color

Minimalist design