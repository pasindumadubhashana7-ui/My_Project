package Controller;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.UUID;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class AddCarDialog extends JDialog {
    private final CarController controller;
    private final Runnable onSuccessCallback;
    private final JTextField brandField = new JTextField();
    private final JTextField modelField = new JTextField();
    private final JTextField priceField = new JTextField();
    private final JComboBox<String> statusCombo = new JComboBox<>(new String[] {"Available", "Rented"});

    public AddCarDialog(Frame owner, CarController controller, Runnable onSuccessCallback) {
        super(owner, "Add New Vehicle", true);
        this.controller = controller;
        this.onSuccessCallback = onSuccessCallback;

        JPanel fields = new JPanel(new GridLayout(4, 2, 5, 5));
        fields.add(new JLabel("Brand:"));
        fields.add(brandField);
        fields.add(new JLabel("Model:"));
        fields.add(modelField);
        fields.add(new JLabel("Price:"));
        fields.add(priceField);
        fields.add(new JLabel("Status:"));
        fields.add(statusCombo);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(event -> saveCar());
        add(fields, BorderLayout.CENTER);
        add(saveButton, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    private void saveCar() {
        String brand = brandField.getText().trim();
        String model = modelField.getText().trim();
        String priceStr = priceField.getText().trim();
        String status = (String) statusCombo.getSelectedItem();

        if (brand.isEmpty() || model.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            String id = "CAR-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            boolean success = controller.addCar(id, brand, model, price, status);
            if (success) {
                JOptionPane.showMessageDialog(this, "Car added successfully! ID: " + id);
                if (onSuccessCallback != null) onSuccessCallback.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add car!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price must be a valid number!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public CarController getController() {
        return controller;
    }
}
