Waybility 

Making accessible navigation easier for everyone.

Waybility is an AI-powered accessibility mapping application designed to help people identify sidewalk accessibility issues and make more informed decisions about their routes. By combining computer vision, community-submitted reports, and accessibility-aware routing, Waybility aims to make navigating public spaces safer and more inclusive.

 The Problem

For many people with mobility challenges, a route that looks accessible on a conventional map may not be accessible in reality. Cracked sidewalks, missing curb ramps, obstacles, and difficult crossings can turn an ordinary trip into a significant challenge.

Most navigation systems prioritize distance and travel time. Waybility explores how accessibility information can help people make better-informed routing decisions.

 Our Solution

Waybility brings AI-assisted sidewalk analysis, community reporting, and accessibility-aware navigation into one application.

 Key Features

 AI-Powered Sidewalk Analysis
Uses computer vision to analyze sidewalk images and identify potential accessibility issues, including curb ramps, obstacles, crosswalks, and surface problems.

 Interactive Accessibility Map
Displays reported accessibility issues geographically, helping users understand potential barriers in their surroundings.

 Accessibility-Aware Routing
Combines mapping data and reported barriers to explore alternative routes that account for accessibility concerns rather than relying solely on the shortest path.

 Community-Powered Reporting
Allows users to submit reports about sidewalk accessibility issues, contributing information that may be missing from conventional maps.

 Report History
Provides a dedicated place to review previously submitted reports and their associated information.

 Inclusive Navigation
Aims to make accessibility information more visible and useful for people with mobility challenges and anyone who benefits from more accessible pedestrian infrastructure.

 How It Works

Waybility combines several technologies to connect image analysis, geographic information, and routing.

Image analysis: A computer vision model analyzes sidewalk imagery to identify and localize potential accessibility problems.

Issue reporting: Users can submit accessibility reports associated with geographic coordinates and images.

Geospatial mapping: Reports are associated with locations on the map.

Route evaluation: Mapping directions, OpenStreetMap data, and nearby accessibility reports inform the evaluation of possible routes and detours.

Accessible navigation: The application uses this information to explore alternative routes that may help users avoid reported barriers.

 Technology Stack

Component

Technology

Android application

Kotlin

User interface

Jetpack Compose

Mapping and places

Google Maps Platform and Google Places API

Backend API

Python and FastAPI

Computer vision

PyTorch and EfficientNet-B0

Image localization

Heatmap-based and localized-coordinate models

Geographic data

OpenStreetMap and Overpass API

Local report storage

Android SharedPreferences

 Architecture

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

 Getting Started

Prerequisites

Android Studio

A compatible Android device or emulator

Python 3

A Google Maps Platform API key with the required APIs enabled

The project's trained model weights and Python dependencies

1. Clone the repository

git clone github.com/btanay041/Waybility

2. Set up the backend

Navigate to the backend directory containing the FastAPI application.

Create and activate a Python virtual environment:

python -m venv .venv

On Windows:

.\.venv\Scripts\Activate.ps1

Install the dependencies listed in the project's requirements.txt:

pip install -r requirements.txt

Start the FastAPI server using the project's actual application module:



Note: The trained model weights must be placed at the paths expected by the backend. Update the module name, model paths, and configuration values to match the repository.

3. Configure the Android application

Open the Android project in Android Studio.

Configure your Google Maps and Places API credentials using the project's configuration.

Set the backend URL to the IP address of the computer running the server when testing on a physical device.

Ensure the phone and development computer can communicate over the local network.

Build and run the application on your Android device or emulator.

For a physical Android device, localhost refers to the phone itself, not your development computer. Use the computer's reachable LAN address for local backend testing.

 Model Development

Waybility's computer vision component uses sidewalk imagery to investigate four accessibility-related categories:

CurbRamp

Crosswalk

Obstacle

SurfaceProblem

The project explores both whole-image classification and localized image analysis, with the goal of identifying not only the type of issue but also where it appears in an image.

Model development includes training, validation, and evaluation using classification metrics and localization error measurements.
