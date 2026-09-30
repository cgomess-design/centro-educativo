package gt.com.ro.devumgapp.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Utilidad para ajustar automáticamente el diseño y desplazamiento en pantallas
 * con formularios cuando el teclado virtual (IME) se muestra u oculta.
 */
public final class KeyboardAdjustHelper {

    private KeyboardAdjustHelper() { }

    /**
     * Configura el ajuste automático del contenido para que nunca quede tapado por el teclado.
     * Busca el ScrollView o contenedor principal y ajusta el padding inferior cuando el teclado se abre,
     * desplazando el campo enfocado para asegurar su completa visibilidad.
     */
    public static void setupKeyboardAdjustment(Activity activity) {
        if (activity == null) return;
        View contentView = activity.findViewById(android.R.id.content);
        if (contentView == null) return;

        contentView.post(() -> {
            ScrollView scrollView = findScrollView(contentView);
            View target = scrollView != null ? scrollView : contentView;
            setupKeyboardAdjustment(activity, target);
        });
    }

    public static void setupKeyboardAdjustment(Activity activity, View targetView) {
        if (activity == null || targetView == null) return;

        final int initialPaddingLeft = targetView.getPaddingLeft();
        final int initialPaddingTop = targetView.getPaddingTop();
        final int initialPaddingRight = targetView.getPaddingRight();
        final int initialPaddingBottom = targetView.getPaddingBottom();

        // 1. Manejo moderno a través de WindowInsets (Android 11+ y compatibilidad con Edge-to-Edge)
        ViewCompat.setOnApplyWindowInsetsListener(targetView, (v, windowInsets) -> {
            Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            int keyboardHeight = imeInsets.bottom;
            int navBarHeight = systemBars.bottom;

            int newBottomPadding = initialPaddingBottom + Math.max(0, keyboardHeight - navBarHeight);
            if (keyboardHeight > 0) {
                newBottomPadding = initialPaddingBottom + keyboardHeight;
            }

            v.setPadding(initialPaddingLeft, initialPaddingTop, initialPaddingRight, newBottomPadding);

            if (keyboardHeight > 0) {
                scrollToFocusedView(activity, v);
            }

            return windowInsets;
        });

        // 2. GlobalLayoutListener como respaldo para compatibilidad universal entre fabricantes y versiones
        targetView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int lastKeypadHeight = 0;

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                targetView.getWindowVisibleDisplayFrame(r);
                int screenHeight = targetView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                // El teclado suele ocupar más del 15% de la altura total
                if (keypadHeight > screenHeight * 0.15) {
                    if (lastKeypadHeight != keypadHeight) {
                        lastKeypadHeight = keypadHeight;
                        targetView.setPadding(
                                initialPaddingLeft,
                                initialPaddingTop,
                                initialPaddingRight,
                                initialPaddingBottom + keypadHeight
                        );
                        scrollToFocusedView(activity, targetView);
                    }
                } else if (lastKeypadHeight > 0) {
                    lastKeypadHeight = 0;
                    targetView.setPadding(
                            initialPaddingLeft,
                            initialPaddingTop,
                            initialPaddingRight,
                            initialPaddingBottom
                    );
                }
            }
        });
    }

    private static void scrollToFocusedView(Activity activity, View container) {
        View focused = activity.getCurrentFocus();
        if (focused == null) return;

        focused.postDelayed(() -> {
            try {
                Rect rect = new Rect();
                focused.getDrawingRect(rect);
                focused.requestRectangleOnScreen(rect, false);

                if (container instanceof ScrollView) {
                    ScrollView sv = (ScrollView) container;
                    Rect offsetRect = new Rect();
                    focused.getDrawingRect(offsetRect);
                    sv.offsetDescendantRectToMyCoords(focused, offsetRect);
                    int scrollY = Math.max(0, offsetRect.top - 60);
                    sv.smoothScrollTo(0, scrollY);
                }
            } catch (Exception ignored) {}
        }, 120);
    }

    private static ScrollView findScrollView(View view) {
        if (view instanceof ScrollView) {
            return (ScrollView) view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                ScrollView sv = findScrollView(group.getChildAt(i));
                if (sv != null) return sv;
            }
        }
        return null;
    }
}
