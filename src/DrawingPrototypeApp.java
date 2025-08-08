import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

abstract class ShapePrototype {
    String getName() {
        return null;
    }
}

class CirclePrototype extends ShapePrototype {
    String name;
    int radius;
    Color color;

    CirclePrototype(String name, int radius, Color color) {
        this.name = name;
        this.radius = radius;
        this.color = color;
    }

    public CircleShape spawnAt(int x, int y) {
        return new CircleShape(this.name + " copy", x, y, radius, color);
    }

    int getRadius() { return radius; }
    Color getColor() { return color; }

    @Override
    public String getName() {
        return name;
    }
}

class CircleShape {
    private String label;
    private int x, y;
    private int radius;
    private Color color;

    CircleShape(String label, int x, int y, int radius, Color color) {
        this.label = label;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color;
    }

    boolean contains(int px, int py) {
        int dx = px - x;
        int dy = py - y;
        return dx*dx + dy*dy <= radius*radius;
    }

    void setRadius(int r) { this.radius = r; }
    void setColor(Color c) { this.color = c; }
    void setLabel(String s) { this.label = s; }

    String getLabel() { return label; }
    int getRadius() { return radius; }
    Color getColor() { return color; }
    int getX() { return x; }
    int getY() { return y; }

    void draw(Graphics2D g, boolean selected) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int d = radius*2;
        g.setColor(color);
        g.fillOval(x - radius, y - radius, d, d);
        g.setStroke(new BasicStroke(selected ? 4f : 2f));
        Color outline = (color != null) ? color.darker().darker() : Color.DARK_GRAY;
        g.setColor(selected ? Color.YELLOW : outline);
        g.drawOval(x - radius, y - radius, d, d);

        g.setFont(g.getFont().deriveFont(Font.BOLD, 12f));
        FontMetrics fm = g.getFontMetrics();
        String lbl = label != null ? label : "";
        int tw = fm.stringWidth(lbl);
        g.setColor(Color.WHITE);
        g.drawString(lbl, x - tw/2, y + fm.getAscent()/2 - 2);
    }

    void moveBy(int dx, int dy) { x += dx; y += dy; }
}

class ShapeManager {
    private final List<CircleShape> shapes = new ArrayList<>();
    private CircleShape selected;
    private final List<ConsumerSelection> selectionListeners = new ArrayList<>();

    void addShape(CircleShape s) {
        shapes.add(s);
        setSelected(s);
    }

    List<CircleShape> getShapes() { return Collections.unmodifiableList(shapes); }

    void setSelected(CircleShape s) {
        this.selected = s;
        for (ConsumerSelection l : selectionListeners) l.selected(s);
    }

    CircleShape getSelected() { return selected; }

    void addSelectionListener(ConsumerSelection l) { selectionListeners.add(l); }
}

/**
 * Registry to store prototypes by name and create clones (spawn shapes).
 */
class PrototypeRegistry {
    private final LinkedHashMap<String, CirclePrototype> prototypes = new LinkedHashMap<>();

    void register(String name, CirclePrototype proto) {
        prototypes.put(name, proto);
    }

    /**
     * Spawn a CircleShape on the canvas centered around canvasW/canvasH with a random offset.
     * Returns the created CircleShape (caller should add it to ShapeManager).
     */
    CircleShape spawnShape(String name, int canvasW, int canvasH) {
        CirclePrototype p = prototypes.get(name);
        if (p == null) throw new IllegalArgumentException("Prototype not found: " + name);
        int x = canvasW/2 + (int)((Math.random()-0.5)*200);
        int y = canvasH/2 + (int)((Math.random()-0.5)*200);
        return p.spawnAt(Math.max(40, x), Math.max(40, y));
    }

    Set<String> keys() {
        return prototypes.keySet();
    }

    void remove(String key) {
        prototypes.remove(key);
    }

    CirclePrototype getPrototypeForEdit(String key) {
        CirclePrototype p = prototypes.get(key);
        if (p == null) return null;
        return new CirclePrototype(p.getName(), p.getRadius(), p.getColor());
    }
}

interface ConsumerSelection { void selected(CircleShape s); }

class PrototypeEditorDialog extends JDialog {
    private CirclePrototype created;
    JTextField nameField;
    JSlider sizeSlider;
    JButton colorBtn;
    private Color selectedColor;

    PrototypeEditorDialog(Frame owner, CirclePrototype editing) {
        super(owner, true);
        setTitle(editing == null ? "Create Prototype" : "Edit Prototype");
        setSize(420, 220);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(new EmptyBorder(10,10,10,10));

        nameField = new JTextField(editing == null ? "New Prototype" : editing.getName());
        main.add(new JLabel("Name:"));
        main.add(nameField);
        main.add(Box.createVerticalStrut(8));

        sizeSlider = new JSlider(10, 150, editing == null ? 50 : editing.getRadius());
        sizeSlider.setMajorTickSpacing(20);
        sizeSlider.setPaintTicks(true);
        sizeSlider.setPaintLabels(true);
        main.add(new JLabel("Radius:"));
        main.add(sizeSlider);
        main.add(Box.createVerticalStrut(8));

        selectedColor = editing == null ? Color.GRAY : editing.getColor();
        colorBtn = new JButton("Choose Color");
        colorBtn.setBackground(selectedColor);
        colorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Prototype Color", selectedColor);
            if (c != null) { selectedColor = c; colorBtn.setBackground(c); }
        });
        main.add(new JLabel("Color:"));
        main.add(colorBtn);

        add(main, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton ok = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        buttons.add(ok);
        buttons.add(cancel);
        add(buttons, BorderLayout.SOUTH);

        ok.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "Name is required."); return; }
            created = new CirclePrototype(name, sizeSlider.getValue(), selectedColor);
            setVisible(false);
        });

        cancel.addActionListener(e -> {
            created = null;
            setVisible(false);
        });
    }

    CirclePrototype getCreatedPrototype() { return created; }
}

class CanvasPanel extends JPanel {
    private Point dragStart;
    private CircleShape draggingShape;
    private final Cursor grabCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private final ShapeManager shapeManager;
    Consumer<String> logger;
    private ConsumerSelection selectionListener;

    CanvasPanel(ShapeManager shapeManager, Consumer<String> logger) {
        this.shapeManager = shapeManager;
        this.logger = logger;
        setPreferredSize(new Dimension(600, 480));

        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                CircleShape picked = pickAt(e.getX(), e.getY());
                if (picked != null) {
                    shapeManager.setSelected(picked);
                    draggingShape = picked;
                    dragStart = e.getPoint();
                    setCursor(grabCursor);
                } else {
                    shapeManager.setSelected(null);
                }
                repaint();
            }

            @Override public void mouseReleased(MouseEvent e) {
                draggingShape = null;
                setCursor(Cursor.getDefaultCursor());
            }

            @Override public void mouseDragged(MouseEvent e) {
                if (draggingShape != null && dragStart != null) {
                    int dx = e.getX() - dragStart.x;
                    int dy = e.getY() - dragStart.y;
                    draggingShape.moveBy(dx, dy);
                    dragStart = e.getPoint();
                    repaint();
                }
            }

            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    CircleShape clicked = pickAt(e.getX(), e.getY());
                    if (clicked != null) {
                        CircleShape copy = new CircleShape(clicked.getLabel() + " ✦", clicked.getX() + 30, clicked.getY() + 30, clicked.getRadius(), clicked.getColor());
                        shapeManager.addShape(copy);
                        if (logger != null) logger.accept("🔁 Cloned shape '" + clicked.getLabel() + "' via double-click.");
                        repaint();
                    }
                }
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(ma);

        shapeManager.addSelectionListener(s -> {
            repaint();
            if (selectionListener != null) selectionListener.selected(s);
        });
    }

    void setSelectionListener(ConsumerSelection l) { this.selectionListener = l; }

    private CircleShape pickAt(int x, int y) {
        List<CircleShape> list = shapeManager.getShapes();
        for (int i = list.size()-1; i >= 0; i--) {
            CircleShape s = list.get(i);
            if (s.contains(x, y)) return s;
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // styled background
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth(), h = getHeight();
        g2.setColor(new Color(40, 44, 52));
        g2.fillRect(0, 0, w, h);

        // subtle radial vignette for style
        for (int i = 0; i < 6; i++) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.06f));
            g2.setColor(new Color(0, 0, 0));
            g2.fillOval(-w/2 + i*30, -h/2 + i*30, w + i*60, h + i*60);
        }

        // draw shapes
        for (CircleShape s : shapeManager.getShapes()) {
            boolean sel = (s == shapeManager.getSelected());
            s.draw(g2, sel);
        }
        g2.dispose();
    }
}

public class DrawingPrototypeApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DrawingPrototypeApp().start());

        ShapePrototype shape = new CirclePrototype("Example", 50, Color.BLUE);
        System.out.println("Shape name: " + shape.getName());
    }

    private JFrame frame;
    private CanvasPanel canvas;
    private DefaultListModel<String> prototypeListModel;
    private JList<String> prototypeJList;
    private JComboBox<String> prototypeCombo;
    private JTextArea logArea;
    private PrototypeRegistry registry;
    private ShapeManager shapeManager;

    private void start() {
        registry = new PrototypeRegistry();
        shapeManager = new ShapeManager();

        // default prototypes
        registry.register("Tiny Red", new CirclePrototype("Tiny Red", 30, Color.RED));
        registry.register("Blue Burst", new CirclePrototype("Blue Burst", 70, new Color(30,144,255)));
        registry.register("Mint Medium", new CirclePrototype("Mint Medium", 50, new Color(152,251,152)));

        buildUI();
        log("✨ App started. Prototype pattern ready — create by cloning prototypes!");
    }

    private void buildUI() {
        frame = new JFrame("🎨 Prototype Drawing Studio — Clone • Edit • Delight");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 700);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        // LEFT - Prototype control panel
        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setBorder(new EmptyBorder(12, 12, 12, 0));
        left.setPreferredSize(new Dimension(260, 0));

        JLabel leftTitle = new JLabel("<html><h2>📦 Prototypes</h2></html>");
        left.add(leftTitle, BorderLayout.NORTH);

        prototypeListModel = new DefaultListModel<>();
        for (String k : registry.keys()) prototypeListModel.addElement(k);

        prototypeJList = new JList<>(prototypeListModel);
        prototypeJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane protoScroll = new JScrollPane(prototypeJList);
        left.add(protoScroll, BorderLayout.CENTER);

        JPanel protoButtons = new JPanel(new GridLayout(0, 1, 6, 6));
        JButton btnClone = new JButton("🪄 Clone Selected");
        JButton btnNew = new JButton("➕ New Prototype");
        JButton btnEdit = new JButton("✏️ Edit Prototype");
        JButton btnRemove = new JButton("🗑 Remove Prototype");

        protoButtons.add(btnClone);
        protoButtons.add(btnNew);
        protoButtons.add(btnEdit);
        protoButtons.add(btnRemove);
        left.add(protoButtons, BorderLayout.SOUTH);

        // CENTER - Canvas
        canvas = new CanvasPanel(shapeManager, this::log);
        canvas.setBackground(new Color(25, 28, 35));
        canvas.setBorder(BorderFactory.createLineBorder(new Color(60, 63, 68), 2));

        // RIGHT - Inspector / Controls
        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setBorder(new EmptyBorder(12, 0, 12, 12));
        right.setPreferredSize(new Dimension(300, 0));

        JLabel rightTitle = new JLabel("<html><h2>🛠 Inspector</h2></html>");
        right.add(rightTitle, BorderLayout.NORTH);

        JPanel inspector = new JPanel();
        inspector.setLayout(new BoxLayout(inspector, BoxLayout.Y_AXIS));
        inspector.setBorder(new EmptyBorder(6, 6, 6, 6));
        right.add(inspector, BorderLayout.CENTER);

        // Prototype clone quick combo
        JPanel quickPanel = new JPanel(new BorderLayout(6, 6));
        prototypeCombo = new JComboBox<>(registry.keys().toArray(new String[0]));
        prototypeCombo.setEditable(false);
        quickPanel.add(new JLabel("Quick clone:"), BorderLayout.WEST);
        quickPanel.add(prototypeCombo, BorderLayout.CENTER);
        JButton quickCloneBtn = new JButton("Clone ▶");
        quickPanel.add(quickCloneBtn, BorderLayout.EAST);
        inspector.add(quickPanel);
        inspector.add(Box.createVerticalStrut(8));

        // Selected shape properties
        inspector.add(new JLabel("Selected Shape:"));
        JLabel selName = new JLabel("— none —");
        selName.setFont(selName.getFont().deriveFont(Font.BOLD, 14f));
        inspector.add(selName);
        inspector.add(Box.createVerticalStrut(6));

        inspector.add(new JLabel("Color:"));
        JButton colorBtn = new JButton("Choose Color");
        inspector.add(colorBtn);
        inspector.add(Box.createVerticalStrut(6));

        inspector.add(new JLabel("Size (radius):"));
        JSlider sizeSlider = new JSlider(10, 150, 50);
        sizeSlider.setMajorTickSpacing(20);
        sizeSlider.setPaintTicks(true);
        sizeSlider.setPaintLabels(true);
        inspector.add(sizeSlider);
        inspector.add(Box.createVerticalStrut(6));

        inspector.add(new JLabel("Label:"));
        JTextField labelField = new JTextField();
        inspector.add(labelField);
        inspector.add(Box.createVerticalStrut(12));

        JButton updateBtn = new JButton("Apply to Selected");
        updateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        inspector.add(updateBtn);

        inspector.add(Box.createVerticalGlue());

        // Bottom - Log
        JPanel bottom = new JPanel(new BorderLayout(6,6));
        bottom.setBorder(new EmptyBorder(0,12,12,12));
        logArea = new JTextArea(6, 10);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        JScrollPane logScroll = new JScrollPane(logArea);
        bottom.add(logScroll, BorderLayout.CENTER);

        // Assemble
        frame.add(left, BorderLayout.WEST);
        frame.add(canvas, BorderLayout.CENTER);
        frame.add(right, BorderLayout.EAST);
        frame.add(bottom, BorderLayout.SOUTH);

        // Event wiring ------------------------------------------------

        btnClone.addActionListener(e -> {
            String key = prototypeJList.getSelectedValue();
            if (key == null) {
                JOptionPane.showMessageDialog(frame, "Select a prototype to clone.", "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            CircleShape created = registry.spawnShape(key, canvas.getWidth(), canvas.getHeight());
            shapeManager.addShape(created);
            canvas.repaint();
            log("🪄 Cloned prototype '" + key + "' → placed on canvas.");
        });

        quickCloneBtn.addActionListener(e -> {
            String key = (String) prototypeCombo.getSelectedItem();
            if (key == null) return;
            CircleShape created = registry.spawnShape(key, canvas.getWidth(), canvas.getHeight());
            shapeManager.addShape(created);
            canvas.repaint();
            log("▶ Quick clone from '" + key + "' added.");
        });

        btnNew.addActionListener(e -> {
            PrototypeEditorDialog dialog = new PrototypeEditorDialog(frame, null);
            dialog.setVisible(true);
            CirclePrototype proto = dialog.getCreatedPrototype();
            if (proto != null) {
                registry.register(proto.getName(), proto);
                prototypeListModel.addElement(proto.getName());
                prototypeCombo.addItem(proto.getName());
                log("➕ New prototype '" + proto.getName() + "' registered.");
            }
        });

        btnEdit.addActionListener(e -> {
            String key = prototypeJList.getSelectedValue();
            if (key == null) {
                JOptionPane.showMessageDialog(frame, "Pick a prototype from the list to edit.", "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            CirclePrototype original = registry.getPrototypeForEdit(key);
            PrototypeEditorDialog dialog = new PrototypeEditorDialog(frame, original);
            dialog.setVisible(true);
            CirclePrototype updated = dialog.getCreatedPrototype();
            if (updated != null) {
                registry.register(updated.getName(), updated); // overwrite or rename
                prototypeListModel.clear();
                for (String k : registry.keys()) prototypeListModel.addElement(k);
                prototypeCombo.removeAllItems();
                for (String k : registry.keys()) prototypeCombo.addItem(k);
                log("✏️ Prototype '" + key + "' updated → '" + updated.getName() + "'.");
            }
        });

        btnRemove.addActionListener(e -> {
            String key = prototypeJList.getSelectedValue();
            if (key == null) return;
            int ok = JOptionPane.showConfirmDialog(frame, "Remove prototype '" + key + "'?","Confirm", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                registry.remove(key);
                prototypeListModel.removeElement(key);
                prototypeCombo.removeItem(key);
                log("🗑 Prototype '" + key + "' removed.");
            }
        });

        // Canvas selection callback
        canvas.setSelectionListener(shape -> {
            if (shape == null) {
                selName.setText("— none —");
                colorBtn.setBackground(null);
                sizeSlider.setValue(50);
                labelField.setText("");
            } else {
                selName.setText(shape.getLabel());
                colorBtn.setBackground(shape.getColor());
                sizeSlider.setValue(shape.getRadius());
                labelField.setText(shape.getLabel());
            }
        });

        // Color chooser button
        colorBtn.addActionListener(e -> {
            CircleShape s = shapeManager.getSelected();
            if (s == null) {
                JOptionPane.showMessageDialog(frame, "Select a shape on the canvas first.", "No shape", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Color chosen = JColorChooser.showDialog(frame, "Choose Color", s.getColor());
            if (chosen != null) {
                s.setColor(chosen);
                colorBtn.setBackground(chosen);
                canvas.repaint();
                log("🎨 Changed color of selected shape.");
            }
        });

        // Size slider live update
        sizeSlider.addChangeListener(e -> {
            CircleShape s = shapeManager.getSelected();
            if (s != null && !sizeSlider.getValueIsAdjusting()) {
                s.setRadius(sizeSlider.getValue());
                canvas.repaint();
                log("🔍 Size adjusted to " + s.getRadius());
            }
        });

        // Label update (apply)
        updateBtn.addActionListener(e -> {
            CircleShape s = shapeManager.getSelected();
            if (s == null) {
                JOptionPane.showMessageDialog(frame, "Select a shape on the canvas first.", "No shape", JOptionPane.WARNING_MESSAGE);
                return;
            }
            s.setLabel(labelField.getText());
            canvas.repaint();
            log("✏️ Label applied: " + labelField.getText());
        });

        // Double-click on a prototype list to quick-clone
        prototypeJList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String key = prototypeJList.getSelectedValue();
                    if (key != null) {
                        CircleShape created = registry.spawnShape(key, canvas.getWidth(), canvas.getHeight());
                        shapeManager.addShape(created);
                        canvas.repaint();
                        log("✨ Double-click clone '" + key + "' created.");
                    }
                }
            }
        });

        frame.setVisible(true);
    }

    private void log(String message) {
        if (logArea != null) {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        } else {
            System.out.println(message);
        }
    }
}


