package com.diffusiondata.example.adapter.human;

import static com.diffusiondata.example.adapter.human.Constraints.at;
import static com.diffusiondata.example.adapter.human.PopClickListener.attach;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.WEST;
import static javax.swing.BorderFactory.createLoweredBevelBorder;
import static javax.swing.KeyStroke.getKeyStroke;
import static javax.swing.SwingConstants.RIGHT;
import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.UIManager.getSystemLookAndFeelClassName;
import static javax.swing.UIManager.setLookAndFeel;
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;

import com.diffusiondata.gateway.framework.ServiceState;
import com.diffusiondata.gateway.framework.StateHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class HumanGui {
    public static final int COMMAND_KEY_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    private static final Logger LOG = LoggerFactory.getLogger(HumanGui.class);
    private final EventHandlers<SendMessageEvent> sendMessageListeners = new EventHandlers<>();


    // GUI components
    private final JFrame frame;
    private final JTextArea messageTextArea;
    private final JButton sendButton;
    private JLabel serviceStateText;

    // Mutable state
    private Supplier<ServiceState> serviceStateSupplier = null;
    private Function<SetStateEvent, ServiceState> setStateListeners = null;

    public HumanGui(String greeting) {
        this.frame = new JFrame(greeting);
        frame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        frame.setSize(400, 300);

        // Left hand panel
        final JPanel sendMessagePanel = new JPanel();
        sendMessagePanel.setLayout(new BorderLayout());

        messageTextArea = new JTextArea();
        messageTextArea.setLineWrap(true);
        messageTextArea.setWrapStyleWord(true);

        sendMessagePanel.add(new JScrollPane(messageTextArea), CENTER);

        sendButton = new JButton("Send Message");
        sendButton.addActionListener((ev) ->
            sendMessageListeners.dispatch(new SendMessageEvent(messageTextArea.getText()))
        );
        sendMessagePanel.add(sendButton, SOUTH);

        // Bind CMD+S to the Send button
        final KeyStroke keyStroke = getKeyStroke(KeyEvent.VK_S, COMMAND_KEY_MASK);
        bindKeyStrokeToAction(messageTextArea, keyStroke, "sendAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendButton.doClick();
            }
        });
        sendButton.setToolTipText(keyStroke.toString());

        // Right hand panel
        final JComponent statusPanel = buildStatusPanel();

        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sendMessagePanel, statusPanel);
        splitPane.setDividerLocation(150);
        splitPane.setOneTouchExpandable(true);

        frame.setLocationRelativeTo(null); // Center on screen
        frame.add(splitPane);
    }

    private JComponent buildStatusPanel() {
        final Insets insets = new Insets(4, 4, 4, 4);
        final JPanel result = new JPanel();
        result.setLayout(new BorderLayout());

        final JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(createLoweredBevelBorder());

        // Status combo box
        final JComboBox<StateHandler.Status> statusCombo = new JComboBox<>(StateHandler.Status.values());

        formPanel.add(new JLabel("Status:", RIGHT), at(0, 0)
            .anchor(EAST)
            .fill(HORIZONTAL)
            .insets(insets)
            .build());

        formPanel.add(statusCombo, at(1, 0)
            .anchor(WEST)
            .fill(HORIZONTAL)
            .insets(insets)
            .weightx(1.0)
            .build());


        formPanel.add(new JLabel("Title:", RIGHT), at(0, 1)
            .anchor(EAST)
            .fill(HORIZONTAL)
            .insets(insets)
            .build());

        final JTextField titleText = new JTextField(10);
        formPanel.add(titleText, at(1, 1)
            .anchor(WEST)
            .fill(HORIZONTAL)
            .insets(insets)
            .weightx(1.0)
            .build());


        formPanel.add(new JLabel("Description:", RIGHT), at(0, 2)
            .anchor(EAST)
            .fill(HORIZONTAL)
            .insets(insets)
            .build());

        final JTextField descriptionText = new JTextField(10);
        formPanel.add(descriptionText, at(1, 2)
            .anchor(WEST)
            .fill(HORIZONTAL)
            .insets(insets)
            .weightx(1.0)
            .build());

        formPanel.add(new JSeparator(SwingConstants.HORIZONTAL), at(1, 3, 2, 1)
            .anchor(WEST)
            .fill(HORIZONTAL)
            .insets(insets)
            .weightx(1.0)
            .build());

        final JLabel serviceStateLabel = new JLabel("Service State");
        formPanel.add(serviceStateLabel, at(0, 4)
            .anchor(WEST)
            .fill(HORIZONTAL)
            .insets(insets)
            .build());

        serviceStateText = new JLabel("unset");
        formPanel.add(serviceStateText, at(1, 4)
            .anchor(WEST)
            .fill(HORIZONTAL)
            .insets(insets)
            .weightx(1.0)
            .build());

        final JPopupMenu menu = new JPopupMenu();
        menu.add(new JMenuItem("Get")).addActionListener((ev) ->
                serviceStateText.setText(this.serviceStateSupplier.get().toString())
        );
        attach(serviceStateText, menu);
        attach(serviceStateLabel, menu);

        result.add(formPanel, CENTER);
        final JButton setStatusButton = new JButton("Set Status");
        setStatusButton.addActionListener((ev) -> {
            if (this.setStateListeners != null) {
                final ServiceState serviceState = this.setStateListeners.apply(new SetStateEvent(
                    (StateHandler.Status)statusCombo.getSelectedItem(),
                    titleText.getText(),
                    descriptionText.getText())
                );
                this.serviceStateText.setText(serviceState.toString());
            }
        });
        result.add(setStatusButton, SOUTH);

        // Bind CMD+T to the Set Status button
        final KeyStroke keyStroke = getKeyStroke(KeyEvent.VK_T, COMMAND_KEY_MASK);
        bindKeyStrokeToAction(result, keyStroke, "sendStatus", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setStatusButton.doClick();
            }
        });
        setStatusButton.setToolTipText(keyStroke.toString());

        return result;
    }

    private static void bindKeyStrokeToAction(JComponent component, KeyStroke keyStroke, String actionKey, Action action) {
        final InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();

        inputMap.put(keyStroke, actionKey);
        actionMap.put(actionKey, action);
    }

    public void setVisible(boolean visible) {
        this.frame.setVisible(visible);
    }
    
    public void setSendEndabled(boolean enabled) {
        this.messageTextArea.setEnabled(enabled);
        this.sendButton.setEnabled(enabled);
    }

    public void addSendEventHandler(Consumer<SendMessageEvent> handler) {
        this.sendMessageListeners.add(handler);
    }

    public void addSetStateEventHandler(Function<SetStateEvent, ServiceState> handler) {
        this.setStateListeners = handler;
    }

    public void setServiceStateProducer(java.util.function.Supplier<ServiceState> supplier) {
        this.serviceStateSupplier = supplier;
    }

    public static void main(String[] args) {
        invokeLater(() -> {
            try {
                setLookAndFeel(getSystemLookAndFeelClassName());
            }
            catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                LOG.warn("Cannot set look and feel", ex);
            }

            final HumanGui gui = new HumanGui("Simple test");
            gui.setVisible(true);
            gui.addSendEventHandler(ev -> LOG.info("Sending: {}", ev));
            gui.addSetStateEventHandler(ev ->{LOG.info("Setting status: {}", ev); return ServiceState.INITIAL;});
            gui.setServiceStateProducer(() -> ServiceState.INITIAL);
        });
    }


}

class SendMessageEvent {
    private final String message;

    public String getMessage() {
        return message;
    }

    public SendMessageEvent(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "SendMessageEvent{" +
            "message='" + message + '\'' +
            '}';
    }
}

class SetStateEvent {
    private final StateHandler.Status status;
    private final String title;
    private final String description;

    public SetStateEvent(StateHandler.Status status, String title, String description) {
        this.status = status;
        this.title = title;
        this.description = description;
    }

    public StateHandler.Status getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "SetStateEvent{" +
            "status=" + status +
            ", title='" + title + '\'' +
            ", description='" + description + '\'' +
            '}';
    }
}

class PopClickListener extends MouseAdapter {
    final JPopupMenu menu;

    private PopClickListener(JPopupMenu menu) {
        this.menu = menu;
    }

    public static void attach(JComponent component, JPopupMenu menu) {
        component.addMouseListener(new PopClickListener(menu));
    }

    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger())
            doPop(e);
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger())
            doPop(e);
    }

    private void doPop(MouseEvent e) {
        menu.show(e.getComponent(), e.getX(), e.getY());
    }
}