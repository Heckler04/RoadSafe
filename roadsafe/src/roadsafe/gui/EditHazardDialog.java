package roadsafe.gui;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import roadsafe.hazard.Hazard;
import roadsafe.util.Validator;
public class EditHazardDialog extends JDialog implements ActionListener {

    private final JComboBox typeBox = new JComboBox(Hazard.TYPES);
    private final JTextField locationField = new JTextField(20);
    private final JTextField descriptionField = new JTextField(20);
    private final JComboBox severityBox = new JComboBox(Hazard.SEVERITIES);
    private final JButton saveButton = new JButton("Save Changes");
    private final JButton cancelButton = new JButton("Cancel");

    private boolean saved = false;
    private String hazardType;
    private String location;
    private String description;
    private String severity;

    public EditHazardDialog(Frame owner, String currentType, String currentLocation,
                            String currentDescription, String currentSeverity) {
        super(owner, "Edit Hazard Record", true);
        typeBox.setSelectedItem(currentType);
        locationField.setText(currentLocation);
        descriptionField.setText(currentDescription);
        severityBox.setSelectedItem(currentSeverity);

        JPanel fieldsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));
        fieldsPanel.add(new JLabel("Hazard Type:"));
        fieldsPanel.add(typeBox);
        fieldsPanel.add(new JLabel("Location:"));
        fieldsPanel.add(locationField);
        fieldsPanel.add(new JLabel("Description:"));
        fieldsPanel.add(descriptionField);
        fieldsPanel.add(new JLabel("Severity:"));
        fieldsPanel.add(severityBox);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        saveButton.addActionListener(this);
        cancelButton.addActionListener(this);
        getRootPane().setDefaultButton(saveButton);

        add(fieldsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveButton) {
            save();
        } else if (e.getSource() == cancelButton) {
            dispose();
        }
    }

    private void save() {
        String loc = locationField.getText().trim();
        String desc = descriptionField.getText().trim();

        String error = Validator.location(loc);
        if (error == null) {
            error = Validator.description(desc);
        }
        if (error != null) {
            JOptionPane.showMessageDialog(this, error,
                    "Check your details", JOptionPane.WARNING_MESSAGE);
            return;
        }

        hazardType = (String) typeBox.getSelectedItem();
        location = loc;
        description = desc;
        severity = (String) severityBox.getSelectedItem();
        saved = true;
        dispose();
    }

    public boolean wasSaved() {
        return saved;
    }

    public String getHazardType() {
        return hazardType;
    }

    public String getHazardLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getSeverity() {
        return severity;
    }
}
