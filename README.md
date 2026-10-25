# 🛡️ SafeVoice — Personal Safety & Family Guardian

**Your safety. Your family. One tap away.**

SafeVoice is an Android personal safety application designed to help women, girls, children, and families stay connected during emergencies. It combines instant SOS alerts, GPS location sharing, emergency voice activation, audio evidence recording, and consent-based family tracking in one platform.

The goal is to make it easier for someone in danger to request help quickly and for trusted family members to respond with the information they need.

## 🚨 Core Features

### 1. One-Tap Emergency SOS
- Activate an emergency alert by pressing the SOS button.
- Notify connected family members and trusted emergency contacts.
- Include the name of the person who triggered the alert.
- Share the latest available location and emergency timestamp.
- Provide quick actions to call the person or open their location on a map.

### 2. Emergency Voice Activation
- Configure a personal emergency phrase, such as "Save me God."
- Detect the configured phrase through a supported voice-activation system.
- Initiate the emergency workflow when a valid trigger is detected.
- Support English and Malayalam as planned language options.
- Provide alternative SOS activation methods if voice detection is unavailable.

*Voice activation depends on supported Android capabilities, permissions, device configuration, and background execution restrictions.*

### 3. Live GPS Location Sharing
- Share the user's location with authorized contacts.
- Provide a map link to the latest available location.
- Update the location during an active emergency when permitted.
- Display location timestamps and accuracy information.
- Handle unavailable GPS signals and network interruptions.

### 4. Automatic Emergency Audio Recording
- Start audio recording during an activated emergency when permitted.
- Store recordings securely.
- Associate recordings with the corresponding emergency event.
- Support secure evidence uploads when authorized and connectivity is available.
- Protect recordings against unauthorized access.

### 5. Family Safety Circle
Create a private family group containing members such as:

- Father
- Mother
- Daughter
- Son
- Trusted friends or relatives

Family members can join through invitations and share their locations according to their permissions.

### 6. Instant Emergency Notifications
When a member activates SOS, connected members can receive an alert such as:

> 🚨 EMERGENCY ALERT — SAFEVOICE
>
> Emergency from: [Member Name]
>
> This person may need immediate assistance.
>
> Location: [View Map]
>
> Status: ACTIVE

Notifications can include actions to view the location, call the person, and acknowledge the emergency.

Push notifications, SMS, and WhatsApp-compatible sharing will depend on the supported integrations and device capabilities.

### 7. Emergency History
- Maintain a record of emergency events.
- Store event timestamps and relevant location information.
- Track notification delivery and acknowledgment where supported.
- Allow users to manage and delete their data according to the retention policy.

## 👨‍👩‍👧 Example Use Case

A daughter installs SafeVoice and connects her account to her family's Safety Circle.

If she feels unsafe, she can:

1. Press the SOS button or activate her configured emergency phrase.
2. Trigger an emergency event.
3. Share her latest available location with authorized contacts.
4. Start emergency audio recording if the required permissions and Android conditions are satisfied.
5. Notify her parents and other selected contacts.

Her parents receive an emergency notification identifying her by name, with a timestamp and a link to her location. They can open the map, call her, and acknowledge the alert.

## 🛠️ Planned Technology Stack

| Component | Technology |
|---|---|
| Android development | Kotlin |
| User interface | Jetpack Compose |
| UI design | Material 3 |
| Architecture | MVVM / Clean Architecture |
| Authentication | Firebase Authentication |
| Backend database | Cloud Firestore |
| Backend processing | Cloud Functions |
| Push notifications | Firebase Cloud Messaging |
| Maps and location | Google Maps SDK and Android location services |
| Audio recording | Android audio APIs |
| Secure evidence storage | Firebase Storage or equivalent |
| Voice activation | On-device keyword detection or supported speech recognition |

The final technology choices may change during implementation based on security, reliability, cost, and Android platform requirements.

## 🏗️ Development Roadmap

- [ ] Project setup and application architecture
- [ ] User registration and authentication
- [ ] Trusted contacts management
- [ ] One-tap SOS interface
- [ ] Backend-connected emergency event creation
- [ ] Push notifications to connected contacts
- [ ] GPS location sharing
- [ ] Family Safety Circle and invitations
- [ ] Emergency voice phrase configuration
- [ ] Supported background voice activation
- [ ] Emergency audio recording
- [ ] Secure evidence storage
- [ ] SMS and supported WhatsApp sharing
- [ ] Emergency history and resolution
- [ ] Privacy controls and data deletion
- [ ] Automated testing and security review
- [ ] Android release preparation

*Checklist items indicate planned work; they should be marked complete only after implementation and testing.*

## 🔐 Privacy and Security

SafeVoice is intended to protect users while respecting their privacy.

The application is designed to:
- Request explicit permissions for microphone and location access.
- Explain when recording or location sharing is active.
- Restrict family location access to authorized members.
- Protect sensitive information using secure authentication and access controls.
- Encrypt network communications and appropriately protect stored recordings.
- Allow users to revoke sharing permissions and manage their data.
- Avoid covert tracking and unauthorized recording.

## ⚠️ Important Limitations

SafeVoice is intended to support personal safety and is not a replacement for emergency services.

- Emergency notifications require an available delivery channel.
- Location accuracy depends on GPS reception and device settings.
- Android may restrict background microphone access and recording.
- WhatsApp messaging is subject to supported integration methods and platform restrictions.
- An emergency alert cannot be guaranteed to reach contacts if the device is offline, powered off, or unable to communicate.

The application must report failures honestly rather than presenting an unsuccessful operation as completed.

## 🚀 Getting Started

Development setup instructions will be added as the project implementation progresses.

The intended setup process includes:

1. Install Android Studio.
2. Clone this repository.
3. Open the project in Android Studio.
4. Configure the required Firebase project and Android application settings.
5. Configure any required Maps SDK credentials.
6. Build and run the application on a supported Android device.
7. Test emergency features using designated test contacts before real-world use.

Never commit private API keys, service-account credentials, signing keys, or other secrets to the repository.

## 🤝 Contributing

Contributions, feature suggestions, bug reports, and security improvements are welcome.

Before submitting changes:
1. Describe the proposed change.
2. Include relevant tests.
3. Avoid introducing unauthorized tracking or insecure handling of personal information.
4. Document any platform limitations.

## 📄 License

The project license has not yet been selected. A license will be added before public distribution.

## ❤️ Our Mission

Our mission is to make personal safety technology more accessible by helping people request assistance quickly, stay connected with trusted family members, and share important emergency information when it matters most.

**SafeVoice — One tap. One alert. A chance to get help.**
