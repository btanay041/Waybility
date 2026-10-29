# Waybility
### Making accessible navigation easier for everyone.

Waybility is an AI-powered accessibility mapping application designed to help people identify sidewalk accessibility issues and make more informed decisions about their routes. By combining computer vision, community-submitted reports, and accessibility-aware routing, Waybility aims to make navigating public spaces safer and more inclusive.

## The Problem

For many people with mobility challenges, a route that looks accessible on a conventional map may not be accessible in reality. Cracked sidewalks, missing curb ramps, obstacles, and difficult crossings can turn an ordinary trip into a significant challenge.

Most navigation systems prioritize distance and travel time. Waybility explores how accessibility information can help people make better-informed routing decisions.

## Our Solution

Waybility brings AI-assisted sidewalk analysis, community reporting, and accessibility-aware navigation into one application.

### Key Features

- **AI-Powered Sidewalk Analysis:** Uses computer vision to analyze sidewalk images and identify potential accessibility issues, including curb ramps, obstacles, crosswalks, and surface problems.
- **Interactive Accessibility Map:** Displays reported accessibility issues geographically, helping users understand potential barriers in their surroundings.
- **Accessibility-Aware Routing:** Combines mapping data and reported barriers to explore alternative routes that account for accessibility concerns rather than relying solely on the shortest path.
- **Community-Powered Reporting:** Allows users to submit reports about sidewalk accessibility issues, contributing information that may be missing from conventional maps.
- **Report History:** Provides a dedicated place to review previously submitted reports and their associated information.
- **Inclusive Navigation:** Aims to make accessibility information more visible and useful for people with mobility challenges and anyone who benefits from more accessible pedestrian infrastructure.

## How It Works

Waybility combines computer vision, geographic information, and routing to connect sidewalk analysis with navigation.

1. **Image analysis:** A computer vision model analyzes sidewalk imagery to identify and localize potential accessibility problems.
2. **Issue reporting:** Users can submit accessibility reports associated with geographic coordinates and images.
3. **Geospatial mapping:** Reports are associated with locations on the map.
4. **Route evaluation:** Mapping directions, OpenStreetMap data, and nearby accessibility reports inform the evaluation of possible routes and detours.
5. **Accessible navigation:** The application uses this information to explore alternative routes that may help users avoid reported barriers.

## Technology Stack

| Component | Technology |
|---|---|
| Android application | Kotlin |
| User interface | Jetpack Compose |
| Mapping and places | Google Maps Platform and Google Places API |
| Backend API | Python and FastAPI |
| Computer vision | PyTorch and EfficientNet-B0 |
| Image localization | Heatmap-based and localized-coordinate models |
| Geographic data | OpenStreetMap and Overpass API |
| Local report storage | Android SharedPreferences |

## Architecture

```text
Android Application
        |
        v
 Image Selection / Capture
        |
        v
 FastAPI Backend
        |
        v
 AI Image Analysis
        |
        v
 Issue Classification
 and Localization

Android Application
        |
        v
 Accessibility Reports
        |
        v
 Google Maps Directions
 + OpenStreetMap Data
        |
        v
 Route Evaluation
 and Detour Generation
        |
        v
 Accessibility-Aware
 Route Suggestions
```

## Getting Started

### Prerequisites

- Android Studio
- A compatible Android device or emulator
- Python 3
- A Google Maps Platform API key with the required APIs enabled
- The project's trained model weights and Python dependencies

### 1. Clone the Repository

```bash
git clone github.com/btanay041/Waybility
cd <YOUR_PROJECT_DIRECTORY>
```

### 2. Set Up the Backend

Navigate to the backend directory containing the FastAPI application.

Create and activate a Python virtual environment:

```bash
python -m venv .venv
```

On Windows:

```powershell
.\.venv\Scripts\Activate.ps1
```

Install the dependencies listed in the project's `requirements.txt`:

```bash
pip install -r requirements.txt
```

Start the FastAPI server using the project's actual application module:

```bash
uvicorn <MODULE_NAME>:app --reload --host 0.0.0.0 --port 8000
```

Replace `<MODULE_NAME>` with the Python module containing the FastAPI `app` instance.

**Note:** The trained model weights must be placed at the paths expected by the backend. Update the module name, model paths, and configuration values to match the repository.

### 3. Configure the Android Application

1. Open the Android project in Android Studio.
2. Configure your Google Maps and Places API credentials using the project's configuration.
3. Set the backend URL to the IP address of the computer running the server when testing on a physical device.
4. Ensure the phone and development computer can communicate over the local network.
5. Build and run the application on your Android device or emulator.

For a physical Android device, `localhost` refers to the phone itself, not your development computer. Use the computer's reachable LAN address for local backend testing.

## Model Development

Waybility's computer vision component uses sidewalk imagery to investigate four accessibility-related categories:

- **CurbRamp**
- **Crosswalk**
- **Obstacle**
- **SurfaceProblem**

The project explores both whole-image classification and localized image analysis, with the goal of identifying not only the type of issue but also where it appears in an image.

Model development includes training, validation, and evaluation using classification metrics and localization error measurements.

## Future Improvements

- Improve localization accuracy across different lighting conditions and sidewalk environments.
- Expand geographic coverage and the availability of verified accessibility reports.
- Incorporate report confidence, freshness, and quality into route evaluation.
- Improve handling of missing or unreliable sidewalk data.
- Optimize backend inference speed and routing performance.
- Add more detailed accessibility preferences and route explanations.
- Evaluate the application with people who have different mobility needs.
- Improve privacy, security, and accessibility across the entire user experience.

## Limitations

Waybility is a developing prototype. AI predictions and community-submitted reports may be incomplete, inaccurate, or outdated. OpenStreetMap coverage and routing data may also vary by location.

A suggested route should not be interpreted as a guarantee that every sidewalk, crossing, or curb is accessible. Users should continue to exercise their own judgment and follow applicable pedestrian safety guidance.

## Contributing

Contributions, feedback, and ideas that help improve accessible navigation are welcome. Potential areas include computer vision, geospatial analysis, Android development, accessibility testing, and data quality.

## Vision

**Accessibility should be part of how we navigate the world, not an afterthought.**

Waybility aims to help bridge the gap between conventional navigation and the real-world accessibility information people need to move through their communities with greater confidence.

---

*Built with the goal of making everyday navigation more inclusive, one sidewalk at a time.*
