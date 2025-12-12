# Restaurant Chef App

Android app for restaurant chefs to receive and prioritize dish orders.

## Project Overview

This app helps chefs manage incoming orders from a restaurant ordering system. It uses a smart scheduling algorithm to ensure all dishes in an order are completed at the same time, so guests receive their food together.

## Key Features

- **Automatic Priority Scheduling**: Dishes are automatically prioritized based on when they need to start cooking
- **Backward Scheduling Algorithm**: Calculates start times by working backward from target finish time
- **Visual Status Indicators**: Color-coded urgency (red=urgent, yellow=soon, green=normal)
- **Real-time Updates**: Priority list automatically updates as time passes
- **Bundle Completion**: Ensures all dishes in an order finish together

## Technology Stack

- **Language**: Java 11
- **Platform**: Android (minSdk 24, targetSdk 36)
- **Backend**: Payara Server with MySQL (managed by separate team)
- **JSON Parsing**: Gson 2.10.1

## Quick Start

### 1. Add Dependencies

Already configured in `app/build.gradle.kts`:
```kotlin
implementation("com.google.code.gson:gson:2.10.1")
```

### 2. Parse Orders from Backend

```java
// Your JSON response from Payara server
String jsonResponse = // ... API call

// Parse with Gson (one line!)
List<OrderBundle> bundles = GsonHelper.parseOrderBundles(jsonResponse);
```

### 3. Calculate Priority List

```java
// Get dishes sorted by priority
List<PrioritizedDish> priorityList =
    DishPriorityScheduler.calculatePriorityList(bundles);

// Display in your UI
for (PrioritizedDish pd : priorityList) {
    Dish dish = pd.getDish();
    String status = DishPriorityScheduler.getStartTimingStatus(
        dish, LocalDateTime.now()
    );
    // Show in RecyclerView
}
```

## Course Types

The app supports these course types (via `modifiedType` field):
- **"rush"** - Highest priority, make immediately, no timing coordination
- **"appetizer"** - First course, coordinated timing within bundle
- **"main"** - Second course, coordinated timing within bundle
- **"dessert"** - Final course, coordinated timing within bundle

## JSON Format Expected

Your backend should return orders in this format:

```json
[{
  "ID": 1,
  "groupID": 42,
  "id": 1,
  "isDone": false,
  "modifyType": "mains",
  "orders": [
    {
      "id": 101,
      "originalID": 1,
      "name": "Pizza Margherita",
      "orderedAt": "2025-12-11T12:34:56",
      "quantity": 2,
      "selectedAllergens": "Gluten,Dairy",
      "specialInstructions": "Extra cheese",
      "comments": "",
      "activeTime": 5,
      "waitingTime": 15
    }
  ]
}]
```

**Important Fields:**
- `modifyType`: **REQUIRED** - Course type ("rush", "appetizer", "mains", "dessert")
- `activeTime`: Minutes for active preparation (chopping, mixing, etc.)
- `waitingTime`: Minutes for passive cooking/waiting (in minutes)
- `orderedAt`: ISO 8601 timestamp (ISO format)
- `groupID`: Links multiple bundles to same table
- `originalID`: Original dish ID from backend

## Project Structure

```
app/src/main/java/com/miun/restaurantchefapp/
├── models/
│   ├── Dish.java              # Single dish with cook times
│   ├── OrderBundle.java       # Group of dishes for one table
│   ├── PrioritizedDish.java   # Dish with priority score
│   └── CourseType.java        # Enum for course types (Rush/Appetizer/Main/Dessert)
├── scheduling/
│   └── DishPriorityScheduler.java  # Core algorithm
├── utils/
│   └── GsonHelper.java        # JSON parsing helper
└── MainActivity.java
```

## Documentation

- **[ALGORITHM_DOCUMENTATION.md](ALGORITHM_DOCUMENTATION.md)** - Detailed explanation of the scheduling algorithm with examples
- **[EXAMPLE_USAGE.md](EXAMPLE_USAGE.md)** - Code examples for integration, RecyclerView adapter, backend communication

## How the Algorithm Works

### Simple Example

**Order arrives at 12:00 PM with:**
- Pizza: 5 min prep + 15 min cooking = 20 min total
- Salad: 8 min prep + 0 min cooking = 8 min total

**Algorithm calculates:**
1. Target finish: 12:00 + 20 min (longest) = **12:20 PM**
2. Pizza start: 12:20 - 20 min = **12:00 PM** (START NOW!)
3. Salad start: 12:20 - 8 min = **12:12 PM** (Start in 12 min)

Both finish at 12:20 PM together

### Priority Formula

```
Priority Score = (courseType × 1000) + (orderTime × 0.3) + (startTime × 0.5) + (completion × 0.2)
```

**Lower score = Higher priority**

Course type dominates all other factors:
- Rush: 0 × 1000 = **0** (always first!)
- Appetizer: 1 × 1000 = **1000**
- Main: 2 × 1000 = **2000**
- Dessert: 3 × 1000 = **3000**

## Next Steps

1. **Connect to Backend**: Implement API calls to Payara server
2. **Create UI**: Build RecyclerView to display priority list (see EXAMPLE_USAGE.md)
3. **Add Actions**: Implement "Start Cooking" and "Mark Complete" buttons
