# Chef Priority Scheduling Algorithm

## Overview
This algorithm ensures that all dishes in an order bundle are completed at the same time, so guests receive their meals together. It uses **backward scheduling** to calculate when each dish should start cooking.

## Key Concepts

### 1. Course Type Priority (CRITICAL)
The algorithm respects traditional meal course order:

```
Rush (0) → Appetizers (1) → Main Courses (2) → Desserts (3)
```

**Course type overrides all other factors!** An appetizer that needs to start in 20 minutes will ALWAYS be prioritized over a main course that needs to start NOW.

**Special: RUSH Orders**
- RUSH orders have highest priority (0) and bypass timing coordination
- All dishes in a RUSH bundle are scheduled to start immediately
- No backward scheduling - just make them ASAP
- Perfect for urgent orders or simple items

### 2. Backward Scheduling
Instead of starting dishes as they arrive, we work backward from a target finish time:

```
Target Finish Time = Current Time + Longest Cook Time in Bundle
Dish Start Time = Target Finish Time - (activeTime + idleTime)
```

### 3. Cook Time Components
Each dish has two time components:
- **activeTime**: Time spent actively preparing (chopping, mixing, assembling)
- **idleTime**: Time spent passively cooking (baking, grilling, waiting)
- **totalCookTime** = activeTime + idleTime

### 4. Priority Calculation
Priority score is calculated using four weighted components:

```java
priorityScore = (courseTypeComponent × 1000.0) +
                (orderTimeComponent × 0.3) +
                (startTimeComponent × 0.5) +
                (completionComponent × 0.2)
```

**Lower score = Higher priority**

#### Components (in order of importance):
1. **Course Type Component (1000× weight - DOMINATES ALL OTHER FACTORS)**
   - Ensures proper meal course order
   - Rush: 0 × 1000 = 0 (highest priority!)
   - Appetizers: 1 × 1000 = 1000
   - Mains: 2 × 1000 = 2000
   - Desserts: 3 × 1000 = 3000
   - This guarantees rush orders are done first, then appetizers before mains, etc.

2. **Order Time Component (0.3 weight)**
   - Earlier orders get priority within same course
   - Calculated as: `-minutesSinceOrdered`
   - Negative value means older orders get lower scores

3. **Start Time Component (0.5 weight)**
   - Dishes that need to start soon get priority
   - Calculated as: `minutesUntilScheduledStart`
   - If negative (overdue), gets high priority
   - If close to zero, gets high priority

4. **Completion Progress Component (0.2 weight)**
   - Bundles that are partially completed get priority to finish them
   - Calculated as: `-(completionPercentage × 100)`
   - Helps ensure bundles don't get stuck half-done

## Example Walkthroughs

### Scenario 1: Course Type Priority (Multiple Bundles for Same Table)

**Table 42 orders at 12:00 PM:**
- **Bundle A (Appetizers)**: Caesar Salad (8 min prep, 0 min cook)
- **Bundle B (Mains)**: Steak (5 min prep, 15 min cook), Pasta (10 min prep, 5 min cook)

**At 12:00 PM, the priority list will be:**

1. **Caesar Salad** (Appetizer, score ~1000)
   - Start: 12:00 PM
   - Finish: 12:08 PM

2. **Steak** (Main, score ~2000)
   - Start: 12:05 PM
   - Finish: 12:25 PM

3. **Pasta** (Main, score ~2000)
   - Start: 12:10 PM
   - Finish: 12:25 PM

**Result**: Chef sees Salad at top of priority list. After finishing the salad, the priority list updates and Steak appears at top (since it needs to start soon). Both mains finish together at 12:25 PM. Perfect!

### Scenario 2: Single Bundle with 3 Dishes

**Current Time:** 12:00 PM

| Dish | Active Time | Idle Time | Total Time |
|------|-------------|-----------|------------|
| Pizza | 5 min | 15 min | 20 min |
| Salad | 8 min | 0 min | 8 min |
| Cake | 3 min | 12 min | 15 min |

### Step 1: Find Longest Cook Time
- Longest = Pizza (20 minutes)

### Step 2: Calculate Target Finish Time
- Target Finish = 12:00 PM + 20 min = **12:20 PM**

### Step 3: Calculate Start Times (Backward from 12:20 PM)

| Dish | Total Time | Start Time | Finish Time |
|------|-----------|------------|-------------|
| Pizza | 20 min | 12:00 PM | 12:20 PM |
| Salad | 8 min | 12:12 PM | 12:20 PM |
| Cake | 15 min | 12:05 PM | 12:20 PM |

### Step 4: Priority Order at 12:00 PM
1. **Pizza** - Start NOW (0 minutes until start)
2. **Cake** - Start in 5 minutes
3. **Salad** - Start in 12 minutes

All dishes finish together at 12:20 PM!

### Scenario 3: RUSH Order

**RUSH order arrives at 12:00 PM:**
- **Bundle C (Rush)**: Quick Soup (3 min prep, 2 min cook), Garlic Bread (2 min prep, 3 min cook)

**At 12:00 PM:**

1. **Quick Soup** (Rush, score ~0)
   - Start: 12:00 PM (NOW!)
   - Finish: 12:05 PM

2. **Garlic Bread** (Rush, score ~0)
   - Start: 12:00 PM (NOW!)
   - Finish: 12:05 PM

**Result**: RUSH orders bypass all timing coordination. Both dishes start immediately and finish when done. No waiting, no scheduling - just ASAP!

**If there were also an Appetizer order at the same time:**
- RUSH items would appear FIRST in priority list (score ~0)
- Appetizer items would appear SECOND (score ~1000)
- Chef always sees RUSH at the top!

## Status Indicators

The algorithm provides visual status for each dish:

| Status | Color | Minutes Until Start |
|--------|-------|-------------------|
| URGENT | Red | ≤ 0 (overdue or now) |
| SOON | Yellow | 1-5 minutes |
| NORMAL | Green | > 5 minutes |

## Implementation Classes

### Core Models
- `Dish.java` - Represents a single dish with cook times
- `OrderBundle.java` - Represents a group of dishes for one table
- `PrioritizedDish.java` - Wrapper that includes priority score

### Scheduler
- `DishPriorityScheduler.java` - Main algorithm implementation
  - `calculatePriorityList()` - Returns sorted list of dishes by priority
  - `getStartTimingStatus()` - Returns human-readable timing status
  - `getTimingStatusColor()` - Returns color code for UI

### Utilities
- `GsonHelper.java` - Simple wrapper for Gson JSON parsing

## Usage Example

```java
// 1. Parse orders from backend JSON using Gson
String jsonResponse = // ... from Payara server
List<OrderBundle> bundles = GsonHelper.parseOrderBundles(jsonResponse);

// 2. Calculate priority list
List<PrioritizedDish> priorityList = DishPriorityScheduler.calculatePriorityList(bundles);

// 3. Display to chef in priority order
for (PrioritizedDish prioritizedDish : priorityList) {
    Dish dish = prioritizedDish.getDish();
    OrderBundle bundle = prioritizedDish.getParentBundle();

    String status = DishPriorityScheduler.getStartTimingStatus(dish, LocalDateTime.now());
    String color = DishPriorityScheduler.getTimingStatusColor(dish, LocalDateTime.now());

    // Display in UI with appropriate color and status
    System.out.println(dish.getName() + " - " + status);
}

// 4. Update priority list periodically (every minute)
// As time passes, dishes automatically move up in priority
```

## Tuning the Algorithm

You can adjust the weights in `DishPriorityScheduler.java`:

```java
private static final double WEIGHT_COURSE_TYPE = 1000.0;  // Course order (DON'T CHANGE!)
private static final double WEIGHT_ORDER_TIME = 0.3;      // Earlier orders
private static final double WEIGHT_START_TIME = 0.5;      // Start urgency
private static final double WEIGHT_COMPLETION = 0.2;      // Bundle progress
```

### Recommendations:
- **DO NOT change WEIGHT_COURSE_TYPE** - this ensures course order is always respected
- **Increase WEIGHT_START_TIME** if dishes are starting too late
- **Increase WEIGHT_ORDER_TIME** if old orders are getting neglected
- **Increase WEIGHT_COMPLETION** if bundles are getting stuck incomplete
- Keep WEIGHT_COURSE_TYPE much larger than other weights (at least 100×)

## Edge Cases Handled

1. **All dishes same cook time** - All start together
2. **Empty bundle** - Skipped in calculation
3. **Completed dishes** - Excluded from priority list
4. **Overdue dishes** - Get highest priority (negative start time)
5. **Single dish in bundle** - Works normally
6. **RUSH orders** - Bypass timing coordination, start immediately
7. **Mixed course types** - Course order always respected (Rush → Appetizer → Main → Dessert)

## Future Enhancements

Potential improvements for more complex scenarios:

1. **Resource constraints** - Account for number of burners/ovens available
2. **Chef specialization** - Different chefs for different dish types
3. **Prep batching** - Group similar dishes to save time
4. **Dynamic timing** - Adjust based on actual completion times
5. **Kitchen capacity management** - Limit number of concurrent dishes being prepared
