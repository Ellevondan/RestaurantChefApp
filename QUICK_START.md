# Quick Start Guide - Restaurant Chef App

## What This App Does

Helps chefs prioritize which dishes to cook and when, ensuring:
- **Course order is respected** (Rush → Appetizers → Mains → Desserts)
- **Dishes in a bundle finish together** (so guests get their food at the same time)
- **RUSH orders are made ASAP** (no timing coordination)

## For Your Backend Team

Your Payara server needs to send orders in this JSON format:

```json
[
  {
    "ID": 1,
    "groupID": 42,
    "modifiedType": "rush",
    "isDone": false,
    "orders": [
      {
        "id": 101,
        "name": "Quick Soup",
        "orderedAt": "2025-12-12T12:00:00",
        "quantity": 1,
        "selectedAllergens": "",
        "specialInstructions": "",
        "comments": "",
        "activeTime": 3,
        "idleTime": 2
      }
    ]
  },
  {
    "ID": 2,
    "groupID": 42,
    "modifiedType": "appetizer",
    "isDone": false,
    "orders": [
      {
        "id": 102,
        "name": "Caesar Salad",
        "orderedAt": "2025-12-12T12:00:00",
        "quantity": 2,
        "selectedAllergens": "Egg,Dairy",
        "specialInstructions": "Dressing on the side",
        "comments": "",
        "activeTime": 8,
        "idleTime": 0
      }
    ]
  },
  {
    "ID": 3,
    "groupID": 42,
    "modifiedType": "main",
    "isDone": false,
    "orders": [
      {
        "id": 103,
        "name": "Steak",
        "orderedAt": "2025-12-12T12:00:00",
        "quantity": 1,
        "selectedAllergens": "",
        "specialInstructions": "Medium rare",
        "comments": "",
        "activeTime": 5,
        "idleTime": 15
      },
      {
        "id": 104,
        "name": "Pasta",
        "orderedAt": "2025-12-12T12:00:00",
        "quantity": 1,
        "selectedAllergens": "Gluten",
        "specialInstructions": "",
        "comments": "",
        "activeTime": 10,
        "idleTime": 5
      }
    ]
  }
]
```

### Required Fields

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `modifiedType` | String | **REQUIRED** Course type | `"rush"`, `"appetizer"`, `"main"`, `"dessert"` |
| `activeTime` | Integer | Minutes for active prep | `5` |
| `idleTime` | Integer | Minutes for passive cooking | `15` |
| `orderedAt` | String (ISO 8601) | When order was placed | `"2025-12-12T12:00:00"` |
| `groupID` | Integer | Links bundles to same table | `42` |

## How It Works - Simple Example

**Table 42 orders at 12:00 PM:**
- Bundle 1 (RUSH): Quick Soup (5 min total)
- Bundle 2 (Appetizer): Caesar Salad (8 min total)
- Bundle 3 (Main): Steak (20 min total), Pasta (15 min total)

**Chef's Priority List at 12:00 PM:**

```
1. Quick Soup (RUSH) - START NOW! 🔴
2. Caesar Salad (Appetizer) - START NOW! 🔴
3. Steak (Main) - Start in 5 min 🟡
4. Pasta (Main) - Start in 10 min 🟢
```

**What Happens:**
- 12:00 - Chef starts Quick Soup + Caesar Salad
- 12:05 - Quick Soup done ✓, Chef starts Steak
- 12:08 - Salad done ✓, served to table
- 12:10 - Chef starts Pasta
- 12:25 - Steak + Pasta done ✓, served together

**Result:** Perfect! Rush done fast, appetizer before main, mains served together.

## Course Types Explained

### 1. RUSH (`modifiedType: "rush"`)
- **Priority**: Highest (0)
- **Behavior**: Start ALL dishes immediately
- **Use for**: Urgent orders, simple items that need to go out fast
- **Timing**: No coordination - just make it ASAP

### 2. APPETIZER (`modifiedType: "appetizer"`)
- **Priority**: High (1000)
- **Behavior**: All dishes finish together
- **Use for**: Starters, first course
- **Timing**: Backward scheduled to finish at same time

### 3. MAIN (`modifiedType: "main"`)
- **Priority**: Medium (2000)
- **Behavior**: All dishes finish together
- **Use for**: Main courses, entrees
- **Timing**: Backward scheduled to finish at same time

### 4. DESSERT (`modifiedType: "dessert"`)
- **Priority**: Low (3000)
- **Behavior**: All dishes finish together
- **Use for**: Desserts, sweets
- **Timing**: Backward scheduled to finish at same time

## Integration Steps

### 1. Add Dependency (Already Done!)
```kotlin
implementation("com.google.code.gson:gson:2.10.1")
```

### 2. Parse JSON from Backend
```java
String json = // ... fetch from Payara API
List<OrderBundle> bundles = GsonHelper.parseOrderBundles(json);
```

### 3. Calculate Priority List
```java
List<PrioritizedDish> priorityList =
    DishPriorityScheduler.calculatePriorityList(bundles);
```

### 4. Display to Chef
```java
for (PrioritizedDish pd : priorityList) {
    Dish dish = pd.getDish();
    OrderBundle bundle = pd.getParentBundle();

    String status = DishPriorityScheduler.getStartTimingStatus(
        dish, LocalDateTime.now()
    );

    // Display: "Pizza Margherita - START NOW!"
    System.out.println(dish.getName() + " - " + status);
}
```

## Testing Without Backend

Use the example JSON in `EXAMPLE_USAGE.md` or create test data:

```java
String testJson = "[{" +
    "\"ID\":1," +
    "\"groupID\":1," +
    "\"modifiedType\":\"rush\"," +
    "\"isDone\":false," +
    "\"orders\":[{" +
        "\"id\":1," +
        "\"name\":\"Test Dish\"," +
        "\"orderedAt\":\"2025-12-12T12:00:00\"," +
        "\"quantity\":1," +
        "\"selectedAllergens\":\"\"," +
        "\"specialInstructions\":\"\"," +
        "\"comments\":\"\"," +
        "\"activeTime\":5," +
        "\"idleTime\":10" +
    "}]" +
"}]";

List<OrderBundle> bundles = GsonHelper.parseOrderBundles(testJson);
```

## Common Questions

**Q: What if backend doesn't send `modifiedType`?**
A: The dish will get type "UNKNOWN" (priority 99000) - shows at bottom of list

**Q: What if `activeTime` or `idleTime` is missing?**
A: Defaults to 0 - dish will be scheduled to start now

**Q: Can one table have multiple bundles?**
A: Yes! Use same `groupID` - e.g., appetizer bundle + main bundle for table 42

**Q: What happens if a RUSH and MAIN order arrive at same time?**
A: RUSH always comes first (score ~0 vs ~2000)

**Q: Do dishes within a RUSH bundle finish together?**
A: No! RUSH bypasses timing - each dish starts NOW and finishes when done

**Q: Do dishes across different bundles coordinate timing?**
A: No! Each bundle schedules independently. Only dishes WITHIN a bundle finish together.

## Next Steps

1. ✅ Code is ready - algorithm implemented
2. 📋 Backend team: Add `modifiedType`, `activeTime`, `idleTime` to API
3. 🎨 Create UI with RecyclerView (see `EXAMPLE_USAGE.md`)
4. 🔌 Connect to Payara API
5. ✨ Add "Start Cooking" and "Mark Complete" buttons
6. 🔄 Add periodic updates (refresh priority list every minute)

## Full Documentation

- **[README.md](README.md)** - Project overview and setup
- **[ALGORITHM_DOCUMENTATION.md](ALGORITHM_DOCUMENTATION.md)** - Deep dive into algorithm with examples
- **[EXAMPLE_USAGE.md](EXAMPLE_USAGE.md)** - Code samples for UI and backend integration

Good luck with your school project! 🍕👨‍🍳
