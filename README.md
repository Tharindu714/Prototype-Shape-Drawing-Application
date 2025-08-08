# 🎨 DrawingPrototypeApp — Prototype Pattern Studio

> **Make beautiful clones, fast.** A compact, delightful Java Swing app that demonstrates the **Prototype design pattern** through an interactive drawing studio. Clone templates (prototypes), place them on a canvas, edit properties, and watch how templates (prototypes) remain pristine while copies are customized.

---

# 🚀 Quick Highlights

* **Single-file friendly**: Easy to include or split — the app ships as a compact Java codebase.
* **Visual & interactive**: Drag, double-click to clone, quick-clone from a list, edit prototypes.
* **Pattern-first**: Built to make the Prototype Pattern obvious and teachable. 🔍

---

# ✨ Features

* Prototype registry to store named templates (size, color, label).
* Clone templates onto a canvas with a click (or double-click a placed shape to clone it).
* Inspector pane: change color, size, and label of selected shapes (live updates).
* Prototype editor: create / edit / remove prototypes.
* Activity log with emoji badges for instant feedback. 🧾

---

# 📘 Prototype Pattern — Theory (in plain words)

The **Prototype Pattern** is a creational design pattern used when creating new objects by copying an existing object (a prototype) rather than by constructing them from scratch. It is especially useful when object construction is expensive or when you want to preserve default settings and simply tweak copies.

**Core ideas**:

* **Prototype**: an object that serves as a template. It can clone itself.
* **Client**: asks the prototype to produce new instances by cloning.
* **Registry (optional)**: stores and organizes prototypes so the client can easily request clones by name.

**Benefits**:

* Fast object creation (no heavy constructors or repeated setup).
* Keeps templates unchanged while allowing copies to diverge.
* Makes runtime customization and dynamic object generation simple.

---

# 🧩 How Prototype Pattern is applied in this project

This project maps the pattern to concrete classes so you can see the pattern in action:

* **`PrototypeRegistry`** — *Registry / manager*. Stores `CirclePrototype` instances by name and spawns new `CircleShape` objects on demand.
* **`CirclePrototype`** — *Prototype (template)*. Holds default attributes (name, radius, color) and can `spawnAt(x,y)` to create a cloned runtime object.
* **`CircleShape`** — *Product / cloned object*. A drawable instance on the canvas. Copies created from prototypes are independent and adjustable.
* **`ShapeManager`** — *Client-side manager*. Maintains placed shapes, selection state, and notifies listeners.
* **`CanvasPanel`** — Visual client: receives spawn requests and displays clones. Also supports double-click cloning (runtime cloning of existing instances).

**Why this is Prototype:** every time you clone from the registry the app **copies** the prototype attributes into a new on-screen `CircleShape` (not a reference to the prototype). That ensures prototypes stay unchanged while copies can be moved, recolored, resized, and labeled independently.

---

# 💻 How to run

1. Ensure you have **JDK 8+** installed.
2. Compile the Java source(s):

```bash
# Clone the repo
git clone https://github.com/Tharindu714/Prototype-Shape-Drawing-Application.git
# Open in your favorite IDE
gradle run   # or mvn compile exec:java
```

3. Run:

```bash
java Prototype-Shape-Drawing-Application
```

---

# 📸 GUI Screenshot

> **GUI screenshot here**

<img width="1366" height="728" alt="shape drawing UI" src="https://github.com/user-attachments/assets/e4acdd27-1831-4f2c-bb63-c4be99900aff" />

---

# 📐 UML Diagram (Paste PlantUML or image)

> **UML diagram here** (PlantUML or exported PNG)

<p align ="center"><img width="434" height="527" alt="image" src="https://github.com/user-attachments/assets/88e2cdd5-0033-4e10-b3fb-32fd4e43fea4" /></p>

---

# 🛠 Code Structure (conceptual)

```
DrawingPrototypeApp (main)         -> builds UI, wires registry & canvas
PrototypeRegistry                  -> stores CirclePrototype (templates)
CirclePrototype                    -> template data + spawnAt(x,y)
CircleShape                        -> runtime drawable clone
ShapeManager                       -> stores placed shapes & selection
CanvasPanel                        -> paints shapes, handles mouse events
PrototypeEditorDialog              -> create/edit prototypes
```

---

# 🧪 Walkthrough (demo)

1. Start the app — default prototypes are registered. ✅
2. Select a prototype and click **Clone** (or double-click a prototype in the list). A new shape appears on the canvas. 🪄
3. Click a placed shape to select it — use the Inspector to change color/size/label. Changes affect only the clone. 🔧
4. Edit prototypes via **Edit Prototype** — subsequent clones will use the updated template while existing clones remain unchanged. 🔒

This flow demonstrates the Prototype Pattern clearly: templates are reused and produce independent clones.

---

# 🧭 Extending the app (ideas)

* Add persistence (save/load prototype registry to JSON). 💾
* Add image-based prototypes or complex composite prototypes (grouped shapes). 🖼️
* Export canvas as PNG or SVG. 📤
* Add undo/redo for edits and clone actions. ↩️

---

# 🤝 Contribution & License

If you use or modify this project, please keep attribution. Feel free to open issues or suggest features. Licensed permissively — include your preferred license file (MIT recommended).

---

# 🙌 Final notes

This project is both a learning tool and a small production-worthy demo: it shows **how prototypes let you spawn complex objects quickly while preserving clean templates** 🎉
