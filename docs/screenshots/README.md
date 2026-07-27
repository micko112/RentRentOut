# Screenshots

This folder contains images referenced from the main `README.md` and from the `wiki/` pages.

## Required images

To make the README render fully, add the following PNG or JPG files to this folder (using the exact filenames):

### Hero (used in the README hero section)

| File | Description | Recommended size |
|---|---|---|
| `hero-banner.png` | Project cover image (logo and title) | 1200 x 630 |
| `home.png` | Home page with hero banner and categories | 1600 x 900 |
| `ad-details.png` | Ad details (gallery, pricing, calendar) | 1600 x 900 |
| `chat.png` | Chat inbox (three columns: conversations, messages, calendar) | 1600 x 900 |
| `create-ad.png` | Ad creation wizard (Step 1 or Step 2) | 1600 x 900 |

### Sections

| File | Description |
|---|---|
| `search-filters.png` | Search view with FiltersSidebar and result list |
| `ad-list-home.png` | Home mode with Latest and five categories |
| `user-profile.png` | Public user profile (ads and reviews) |
| `my-ads.png` | "My ads" with promotion badges and expiry info |
| `contracts.png` | Contract list (incoming / outgoing) |
| `credit-page.png` | Credit page (balance, packages, history) |
| `notifications.png` | Notification centre |
| `admin-dashboard.png` | Admin dashboard with six statistics cards |
| `admin-reports.png` | Admin report moderation view |
| `promotion-modal.png` | Modal for choosing a promotion package |
| `rental-calendar.png` | Availability calendar component |
| `review-form.png` | Rating form (three questions) |
| `email-template.png` | Sample HTML email (verification or contract) |
| `cookie-banner.png` | GDPR cookie banner |
| `login.png` | Login page with social buttons |
| `register.png` | Registration page with terms-of-service checkbox |

### ML / AI (used in `wiki/ML-Service.md`)

| File | Description |
|---|---|
| `ml-architecture.png` | Diagram: Angular -> Spring Boot -> FastAPI ML service |
| `ml-training-curve.png` | Screenshot of the accuracy-per-epoch chart (from Jupyter) |
| `ml-prediction-demo.png` | Prediction demo inside the wizard (auto-suggest in action) |
| `chatbot-conversation.png` | Screenshot of a chatbot conversation |

### Mobile

| File | Description |
|---|---|
| `mobile-home.png` | Mobile home page (<= 900 px) |
| `mobile-chat.png` | Mobile chat view |

## How to take screenshots

1. Run the application locally (`docker-compose up --build`).
2. Open Chrome DevTools -> Device Toolbar -> 1600 x 900 for desktop shots.
3. For mobile shots: 390 x 844 (iPhone 14) or 414 x 896.
4. For HTML emails: open your inbox, click the email, capture the screenshot.
5. Save each PNG in this folder using the exact filenames from the tables above.
6. Commit and push.

> If an image is missing, the README or Wiki will show a broken image placeholder — a clear signal that it has not been added yet.
