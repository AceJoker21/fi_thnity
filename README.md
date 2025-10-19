# Fi Thnity  (On My Way)

**Save Time, Save Tunisia** - A community-driven carpooling and ride-sharing application for Tunisia.

## 🚗 About

Fi Thnity connects Tunisians heading in the same direction, making transportation easier, faster, and more affordable. Whether you need a ride or are offering one, Fi Thnity helps you find travel companions for your journey.

## ✨ Features

- 📱 **Phone Authentication** - Secure login via Firebase Phone Auth with OTP verification
- 🗺️ **MapLibre Integration** - Interactive maps powered by MapTiler
- 🚖 **Ride Broadcasting** - Post ride requests or offers with transport type selection
- 📍 **Location Selection** - Interactive map-based location picker with reverse geocoding
- 👥 **Multiple Transport Types** - Taxi, Taxi Collectif, Private Car, Metro, Bus
- 🔍 **Rides Discovery** - Browse and filter active ride requests and offers in real-time
- 👤 **Profile Management** - View and edit profile with photo upload
- 🎨 **Material Design 3** - Modern, Tunisian-inspired UI with custom color palette
- 🔥 **Firebase Backend** - Realtime Database (Europe West 1), Authentication, Storage, Cloud Messaging

## 🎨 Design

The app features a Tunisian-inspired color palette:
- **Bleu Saphir Tunisien** (#006D9C) - Primary
- **Jaune Sable du Sahel** (#FFD54F) - Secondary
- **Rouge Médina** (#D62828) - Accent

## 🛠️ Tech Stack

- **Language:** Java
- **Min SDK:** 29 (Android 10)
- **Target SDK:** 36
- **Maps:** MapLibre GL Native 10.0.2 with MapTiler
- **Backend:** Firebase (Auth, Realtime Database, Cloud Messaging)
- **UI:** Material Design 3
- **Build Tool:** Gradle 8.13

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 11 or higher
- MapTiler API Key (free tier available)
- Firebase project with google-services.json

### Setup

1. **Clone the repository**
   ```bash
   git clone git@github.com:medb2m/fi_thnity.git
   cd fi_thnity
   ```

2. **Configure API Keys**

   Copy the example gradle.properties:
   ```bash
   cp gradle.properties.example gradle.properties
   ```

   Edit `gradle.properties` and add your MapTiler API key:
   ```properties
   MAPTILER_API_KEY="your_api_key_here"
   ```

   Get your free API key from [MapTiler Cloud](https://cloud.maptiler.com/)

3. **Configure Firebase**

   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Enable Phone Authentication
   - Create Realtime Database (use **Europe West 1** region)
     - Database URL: `https://fi-thnity-11a68-default-rtdb.europe-west1.firebasedatabase.app`
   - Enable Firebase Storage for profile photos
   - Download `google-services.json` and place it in `app/` directory

   **Important**: The app is configured to use Firebase Realtime Database in the Europe West 1 region. If you create a database in a different region, you'll need to update the database URL in all activities and fragments.

4. **Build and Run**
   ```bash
   ./gradlew build
   ```

   Open in Android Studio and run on emulator or device.

## 📱 App Structure

### Screens
- **Splash & Onboarding** - Welcome screens with app introduction
- **Phone Authentication** - Login with phone number and OTP verification
- **Profile Setup/Edit** - Create or update user profile with photo upload
- **Home** - Interactive map view with quick action buttons
- **Broadcast Ride** - Post ride request or offer with location selection
- **Rides** - Browse and filter active ride requests and offers
- **Community** - Social feed (in progress)
- **Profile** - View user profile, stats, and settings

### Key Components
- `activities/` - All activity classes
- `fragments/` - Fragment implementations (Home, Rides, Community, Profile)
- `models/` - Data models (Ride, Location, TransportType, User)
- `adapters/` - RecyclerView adapters
- `utils/` - Utility classes

## 🔐 Security

**Important:** Never commit sensitive files to Git!

The following files are gitignored and must be configured locally:
- `gradle.properties` - Contains API keys
- `google-services.json` - Firebase configuration
- `*.keystore` - Signing keys

## 📝 Development Status

**Completed (75%):**
- ✅ Onboarding screens with ViewPager2
- ✅ Phone authentication with Firebase Auth
- ✅ OTP verification with countdown timer
- ✅ Profile creation and editing
- ✅ Profile photo upload to Firebase Storage
- ✅ Real user data display from Firebase
- ✅ Home screen with bottom navigation
- ✅ MapLibre integration with MapTiler
- ✅ Location selection with reverse geocoding
- ✅ Broadcast ride screen (requests and offers)
- ✅ Rides viewing with real-time Firebase updates
- ✅ Rides filtering (All/Requests/Offers)
- ✅ Material Design 3 UI throughout
- ✅ Proper Firebase Database region configuration

**In Progress:**
- 🔄 Ride matching algorithm
- 🔄 Real-time location tracking service
- 🔄 Community feed
- 🔄 Chat functionality
- 🔄 Push notifications

## 🤝 Contributing

This is a student project for ESPRIT. Contributions are welcome!

## 📄 License

This project is for educational purposes.

## 👥 Team

Developed at ESPRIT - École Supérieure Privée d'Ingénierie et de Technologies

---

** On My Way** 🇹🇳
