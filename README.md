# Asra 🛡️

**A Privacy-First Safety Companion for Women**

Asra is a mobile safety system that watches for unusual patterns in how you use your phone during emergencies—without watching *what* you do. No AI black boxes. No constant listening. Just transparent, rule-based protection that kicks in only when you need it.

---

## Why Asra Exists

Traditional safety apps have a problem: they either wait for you to press a panic button (which assumes you can), or they use creepy always-on monitoring that feels invasive. Many rely on ML algorithms that make decisions you can't understand or trust.

**We built Asra differently.**

When you're in danger, your phone behavior changes in subtle but detectable ways—rapid screen toggling, erratic app switching, sudden route changes. Asra watches for these patterns, not your conversations or keystrokes. It's like having a friend who notices when something feels off, without reading over your shoulder.

---

## How It Works

### Shadow Mode 🌙

Think of it as a protective layer you turn on when you need extra security—walking alone at night, taking an Uber in an unfamiliar area, or meeting someone new.

**When Shadow Mode is ON:**
- Your phone quietly monitors interaction patterns (screen taps, app switches, movement)
- No microphone access unless you're being asked "Are you safe?"
- All processing happens on your device
- You can turn it off anytime with one tap

**When Shadow Mode is OFF:**
- Zero monitoring. Zero data collection.
- The system doesn't exist until you activate it.

---

## What Asra Watches For

Asra tracks six behavioral signals that might indicate distress. **None of these involve reading your content:**

1. **Frantic Screen Toggling** → Turning your screen on/off rapidly (8+ times in a minute)
2. **Chaotic App Switching** → Jumping between apps unusually fast (10+ switches/min)
3. **Frozen Behavior** → Screen is on but you're not touching it for way longer than normal
4. **Erratic Phone Movement** → Device rotating wildly (20+ times in 30 seconds)
5. **Unexpected Route Changes** → Suddenly veering off your usual path or navigation route
6. **Desperate Battery Usage** → Heavily using your phone when battery is critically low (<10%)

Each signal gets a score from 0 to 1. Scores fade over time if your behavior normalizes. **No single signal triggers an alert**—Asra waits for multiple patterns to persist before asking if you're okay.

---

## The Escalation Flow

Asra uses a state machine with four levels of awareness:

```
NORMAL → OBSERVING → UNSTABLE → CONFIRMED
```

### 🟢 NORMAL
Everything looks fine. System is quiet.

### 🟡 OBSERVING
Asra noticed something unusual (like a few odd app switches), but it's waiting to see if it continues. No alerts yet.

### 🟠 UNSTABLE
Multiple signals are active and persisting. **You get an alert:**

> "We noticed unusual phone activity. Are you safe?"

You have 2 minutes to respond:
- **"I'm Safe"** → System resets to normal
- **"Send Help"** → Immediate escalation
- **No response** → Asra assumes you can't respond and escalates automatically

### 🔴 CONFIRMED
Help is on the way:
- Your trusted contacts get an SMS with your location
- Live location sharing starts automatically
- (Optional) Emergency services notified if you enabled it

You can still cancel if it's a false alarm—up to 5 minutes after escalation.

---

## Voice Assist (Last Resort Only)

If you're in the UNSTABLE state and don't respond to the on-screen alert, Asra activates a **one-time voice check** as a last confirmation before escalating.

**How it works:**
- Your phone listens for specific distress words: *"help," "bachao," "chodho," "save me"*
- If you say any keyword **2+ times within 10 seconds**, escalation happens immediately
- Audio is processed **on-device only**—nothing is recorded or sent anywhere
- If no keywords detected and you still don't respond, escalation proceeds anyway

This isn't voice recognition AI. It's a simple keyword matcher that runs locally, used only when you can't tap your screen.

---

## What Makes Asra Different

| Feature | Traditional Safety Apps | Asra |
|---------|------------------------|------|
| **Trigger Method** | Manual panic button only | Automatic detection + manual button |
| **Detection Logic** | Single event triggers | Multiple signals must persist |
| **Privacy** | Often uses ML on behavior | Rule-based, no content access |
| **Explainability** | "AI detected danger" (vague) | Shows exactly which rules fired |
| **False Alarms** | High (single-trigger) | Lower (multi-signal correlation) |
| **Always Listening?** | Some apps, yes | Only during final verification |

---

## Privacy Promise

**What Asra NEVER does:**
- ❌ Read your messages, emails, or typed content
- ❌ Record your conversations
- ❌ Access your camera without permission
- ❌ Send data to cloud servers for "AI training"
- ❌ Share your data with third parties
- ❌ Monitor you when Shadow Mode is OFF

**What Asra does collect (only in Shadow Mode):**
- ✅ Timestamps of screen on/off events
- ✅ App foreground/background transitions (app names only)
- ✅ Device orientation changes
- ✅ Location updates (only during alerts)
- ✅ Battery level

All metadata stays in your phone's RAM and is erased when you turn Shadow Mode off.

---

## Tech Stack

**Mobile App:**
- **Platform:** Android (Kotlin) | iOS (Swift) planned
- **Architecture:** Foreground service with sensor event listeners
- **Detection:** Pure rule-based logic (no ML frameworks)
- **Storage:** In-memory only (volatile)
- **Permissions:** Location, Motion Sensors, Notifications

**Backend (for escalation only):**
- **Location Sharing:** Node.js + PostgreSQL
- **Messaging:** Twilio (SMS), SendGrid (Email)
- **Hosting:** AWS (encrypted at rest and in transit)

---

## Current Limitations

Asra is a **research prototype**, not a bulletproof safety guarantee. Here's what it can't do:

- ❌ **Can't predict danger before it happens** (we're not psychic)
- ❌ **Can't detect danger in all scenarios** (false negatives will happen)
- ❌ **Can't work perfectly in background on iOS** (Apple limits background tasks to 30 seconds)
- ❌ **Can't replace calling 911** (use manual SOS for immediate threats)
- ❌ **Can't function without battery** (keep your phone charged!)

**We're building a helpful layer, not a magic shield.** Always trust your instincts and use manual SOS if you feel unsafe.

---

## Getting Started

### Installation (Prototype)
```bash
# Clone the repo
git clone https://github.com/yourusername/asra.git
cd asra

# Install dependencies
npm install  # or yarn install

# Run on Android
npx react-native run-android

# Run on iOS (coming soon)
npx react-native run-ios
```

### First-Time Setup
1. Open Asra and create an account
2. Add at least one trusted contact (verified phone number)
3. Grant location and sensor permissions
4. Toggle Shadow Mode ON when you want protection
5. Review the explanation of each detection rule

### Testing (Safe Simulation)
Use the **"Simulate Instability"** button in settings to trigger fake anomalies and see how the system responds. This helps you understand the escalation flow without putting yourself in danger.

---

## Roadmap

**Version 1.0** (Current Prototype)
- ✅ 6-rule detection system
- ✅ FSM with 4 states
- ✅ SMS/email escalation
- ✅ Android support

**Version 2.0** (Planned - 12 months)
- [ ] iOS support
- [ ] Adaptive rule weights (personalized thresholds)
- [ ] Wearable integration (Apple Watch, Wear OS)
- [ ] Group safety mode (share location with friends proactively)
- [ ] International emergency numbers

**Version 3.0** (Aspirational - 18 months)
- [ ] Optional ML supplement (still explainable)
- [ ] Smart home integration (auto-unlock door, turn on lights)
- [ ] Community safety heatmap

---

## Contributing

We welcome contributions, but please understand:
- This is a **safety-critical system**—bugs can have serious consequences
- All PRs must include tests and documentation
- Privacy-invasive features will be rejected
- Read `CONTRIBUTING.md` before submitting

**Areas we need help:**
- iOS native development
- Battery optimization techniques
- Accessibility features (VoiceOver, TalkBack)
- Localization (Hindi, Spanish, Arabic, etc.)
- UI/UX for high-stress scenarios

---

## Research & Inspiration

This project draws from research in:
- Human-computer interaction during crisis
- Behavioral biometrics (used ethically)
- False alarm mitigation in security systems
- Trauma-informed design

**Key papers that influenced Asra:**
- "Freeze Response in Dangerous Situations" (Psychology of Trauma)
- "False Positive Rates in Personal Safety Systems" (CHI 2023)
- "Privacy-Preserving Behavioral Monitoring" (USENIX Security)

---

## Disclaimer

⚠️ **Important Legal Notice:**

Asra is a **prototype research project** and is provided "as-is" without warranty. It is **not a substitute for emergency services, personal safety training, or professional security.**

- The system may fail to detect danger (false negatives)
- The system may alert when you're safe (false positives)
- Battery drain, network issues, or OS restrictions may prevent escalation
- **Always call emergency services directly if you are in immediate danger**

By using Asra, you acknowledge that the developers are not liable for harm, injury, or loss resulting from system failures or limitations.

---

## License

MIT License (see `LICENSE.md`)

**Free for educational, research, and non-commercial use.** If you want to use Asra in a commercial product, please contact us.

---

## Contact & Support

- **Issues:** [GitHub Issues](https://github.com/srujakwarbhuvan/Team-Rogue-/issues)
- **Discussions:** [GitHub Discussions](https://github.com/srujakwarbhuvan/Team-Rogue-/discussions)
- **Email:** srujakwarbhuvan06@gmail.com,yashthokne184@gmail.com,prasannatawandhe@gmail.com
  


---

## Acknowledgments

Built with care by a team that believes technology should empower, not surveil.

Special thanks to:
- The women who shared their safety concerns and shaped this design
- Open-source communities for privacy-preserving tools
- Researchers working on trauma-informed technology

**Stay safe. Stay in control. 🛡️**
