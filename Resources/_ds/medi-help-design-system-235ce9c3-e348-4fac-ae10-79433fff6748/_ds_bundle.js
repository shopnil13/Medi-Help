/* @ds-bundle: {"format":4,"namespace":"MediHelpDesignSystem_235ce9","components":[{"name":"Button","sourcePath":"components/core/Button.jsx"},{"name":"IconButton","sourcePath":"components/core/IconButton.jsx"},{"name":"ProvenanceTag","sourcePath":"components/data/ProvenanceTag.jsx"},{"name":"Badge","sourcePath":"components/feedback/Badge.jsx"},{"name":"Dialog","sourcePath":"components/feedback/Dialog.jsx"},{"name":"Snackbar","sourcePath":"components/feedback/Snackbar.jsx"},{"name":"Checkbox","sourcePath":"components/forms/Checkbox.jsx"},{"name":"RadioGroup","sourcePath":"components/forms/RadioGroup.jsx"},{"name":"Select","sourcePath":"components/forms/Select.jsx"},{"name":"Switch","sourcePath":"components/forms/Switch.jsx"},{"name":"TextField","sourcePath":"components/forms/TextField.jsx"},{"name":"BottomNavBar","sourcePath":"components/navigation/BottomNavBar.jsx"},{"name":"TopAppBar","sourcePath":"components/navigation/TopAppBar.jsx"},{"name":"Card","sourcePath":"components/surfaces/Card.jsx"}],"sourceHashes":{"components/core/Button.jsx":"3e1af7b68377","components/core/IconButton.jsx":"bf099f9ddfd7","components/data/ProvenanceTag.jsx":"98126a093bae","components/feedback/Badge.jsx":"c989e58fcd15","components/feedback/Dialog.jsx":"07569165d1eb","components/feedback/Snackbar.jsx":"8083631bc494","components/forms/Checkbox.jsx":"f868f21af625","components/forms/RadioGroup.jsx":"b1259b3a56a0","components/forms/Select.jsx":"2f6dd7c15816","components/forms/Switch.jsx":"0cc37371c9ad","components/forms/TextField.jsx":"20e99e48b9d7","components/navigation/BottomNavBar.jsx":"17ec94342453","components/navigation/TopAppBar.jsx":"2a7754a6df8c","components/surfaces/Card.jsx":"f1cc41f662f6","ui_kits/android_app/HomeScreen.jsx":"002d4d033afd","ui_kits/android_app/InsightsScreen.jsx":"610da6ac8259","ui_kits/android_app/MedicinesScreen.jsx":"ba97156ad478","ui_kits/android_app/SettingsScreen.jsx":"77e3adbcbdb2","ui_kits/android_app/UploadReviewScreen.jsx":"c96278f6fe07","ui_kits/android_app/VitalsScreen.jsx":"88861bdaf82c","ui_kits/android_app/android-frame.jsx":"70c8c3059eeb"},"inlinedExternals":[],"unexposedExports":[]} */

(() => {

const __ds_ns = (window.MediHelpDesignSystem_235ce9 = window.MediHelpDesignSystem_235ce9 || {});

const __ds_scope = {};

(__ds_ns.__errors = __ds_ns.__errors || []);

// components/core/Button.jsx
try { (() => {
const base = {
  fontFamily: 'var(--font-display)',
  fontWeight: 'var(--weight-semibold)',
  border: 'none',
  cursor: 'pointer',
  borderRadius: 'var(--radius-md)',
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  gap: '8px',
  transition: 'background var(--duration-fast) var(--ease-standard), transform var(--duration-fast) var(--ease-standard), opacity var(--duration-fast) var(--ease-standard)',
  whiteSpace: 'nowrap'
};
const sizes = {
  lg: {
    height: '56px',
    padding: '0 28px',
    fontSize: 'var(--text-body-lg)'
  },
  md: {
    height: '48px',
    padding: '0 22px',
    fontSize: 'var(--text-body)'
  },
  sm: {
    height: '40px',
    padding: '0 16px',
    fontSize: 'var(--text-body-sm)'
  }
};
const variants = {
  primary: {
    background: 'var(--color-primary)',
    color: 'var(--text-on-primary)',
    boxShadow: 'var(--shadow-1)'
  },
  secondary: {
    background: 'var(--color-secondary)',
    color: 'var(--color-primary)',
    border: '2px solid var(--color-secondary-border)'
  },
  ghost: {
    background: 'transparent',
    color: 'var(--color-primary)'
  },
  danger: {
    background: 'var(--red-800)',
    color: 'var(--text-on-primary)'
  }
};
const hoverBg = {
  primary: 'var(--color-primary-hover)',
  secondary: 'var(--color-secondary-hover)',
  ghost: 'var(--color-accent-blush)',
  danger: 'var(--red-900)'
};

/** Button — the app's primary tappable action. Large by default; sized for elderly/low-vision users (56px min height). */
function Button({
  variant = 'primary',
  size = 'lg',
  icon,
  disabled = false,
  fullWidth = false,
  children,
  onClick
}) {
  const [hover, setHover] = React.useState(false);
  const [active, setActive] = React.useState(false);
  const style = {
    ...base,
    ...sizes[size],
    ...variants[variant],
    width: fullWidth ? '100%' : undefined,
    opacity: disabled ? 0.45 : 1,
    cursor: disabled ? 'not-allowed' : 'pointer',
    background: !disabled && hover ? hoverBg[variant] : variants[variant].background,
    transform: !disabled && active ? 'scale(0.97)' : 'scale(1)'
  };
  return /*#__PURE__*/React.createElement("button", {
    style: style,
    disabled: disabled,
    onClick: onClick,
    onMouseEnter: () => setHover(true),
    onMouseLeave: () => {
      setHover(false);
      setActive(false);
    },
    onMouseDown: () => setActive(true),
    onMouseUp: () => setActive(false)
  }, icon ? /*#__PURE__*/React.createElement("span", {
    className: "material-symbols-outlined",
    style: {
      fontSize: '22px'
    }
  }, icon) : null, children);
}
Object.assign(__ds_scope, { Button });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/Button.jsx", error: String((e && e.message) || e) }); }

// components/core/IconButton.jsx
try { (() => {
/** IconButton — circular tap target for a single icon-only action (back, close, more). Minimum 48px hit area. */
function IconButton({
  icon,
  size = 48,
  variant = 'ghost',
  label,
  onClick,
  disabled = false
}) {
  const [hover, setHover] = React.useState(false);
  const bg = variant === 'filled' ? hover ? 'var(--color-primary-hover)' : 'var(--color-primary)' : hover ? 'var(--color-accent-blush)' : 'transparent';
  const color = variant === 'filled' ? 'var(--text-on-primary)' : 'var(--text-primary)';
  return /*#__PURE__*/React.createElement("button", {
    "aria-label": label,
    title: label,
    disabled: disabled,
    onClick: onClick,
    onMouseEnter: () => setHover(true),
    onMouseLeave: () => setHover(false),
    style: {
      width: size,
      height: size,
      borderRadius: 'var(--radius-full)',
      border: 'none',
      background: bg,
      color,
      display: 'inline-flex',
      alignItems: 'center',
      justifyContent: 'center',
      cursor: disabled ? 'not-allowed' : 'pointer',
      opacity: disabled ? 0.4 : 1,
      transition: 'background var(--duration-fast) var(--ease-standard)'
    }
  }, /*#__PURE__*/React.createElement("span", {
    className: "material-symbols-outlined",
    style: {
      fontSize: Math.round(size * 0.5)
    }
  }, icon));
}
Object.assign(__ds_scope, { IconButton });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/IconButton.jsx", error: String((e && e.message) || e) }); }

// components/data/ProvenanceTag.jsx
try { (() => {
/**
 * ProvenanceTag — small "Date · Source" label. Intentional addition (not a
 * generic UI primitive) required by product rules: every health/vital record
 * must show its date and source (Manual, Lab Report, Health Connect, AI-extracted).
 */
const sourceIcon = {
  Manual: 'edit',
  'Lab Report': 'description',
  'Health Connect': 'watch',
  'AI-extracted': 'auto_awesome'
};
function ProvenanceTag({
  date,
  source
}) {
  return /*#__PURE__*/React.createElement("span", {
    style: {
      display: 'inline-flex',
      alignItems: 'center',
      gap: '6px',
      color: 'var(--text-secondary)',
      fontFamily: 'var(--font-body)',
      fontSize: 'var(--text-body-sm)'
    }
  }, /*#__PURE__*/React.createElement("span", {
    className: "material-symbols-outlined",
    style: {
      fontSize: '16px'
    }
  }, sourceIcon[source] || 'info'), date, " \xB7 ", source);
}
Object.assign(__ds_scope, { ProvenanceTag });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/data/ProvenanceTag.jsx", error: String((e && e.message) || e) }); }

// components/feedback/Badge.jsx
try { (() => {
const statusMap = {
  normal: {
    bg: 'var(--status-success-bg)',
    fg: 'var(--status-success)',
    label: 'Normal'
  },
  low: {
    bg: 'var(--status-warning-bg)',
    fg: 'var(--status-warning)',
    label: 'Low'
  },
  high: {
    bg: 'var(--status-critical-bg)',
    fg: 'var(--status-critical)',
    label: 'High'
  },
  info: {
    bg: 'var(--status-info-bg)',
    fg: 'var(--status-info)',
    label: 'Info'
  }
};

/** Badge — small status pill. Used for lab/vital status (normal/low/high) or generic tags. */
function Badge({
  status = 'info',
  children
}) {
  const s = statusMap[status] || statusMap.info;
  return /*#__PURE__*/React.createElement("span", {
    style: {
      display: 'inline-flex',
      alignItems: 'center',
      gap: '6px',
      background: s.bg,
      color: s.fg,
      fontFamily: 'var(--font-display)',
      fontWeight: 'var(--weight-semibold)',
      fontSize: 'var(--text-body-sm)',
      padding: '5px 14px',
      borderRadius: 'var(--radius-full)'
    }
  }, children || s.label);
}
Object.assign(__ds_scope, { Badge });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/Badge.jsx", error: String((e && e.message) || e) }); }

// components/feedback/Dialog.jsx
try { (() => {
/**
 * Dialog — modal confirmation. Used before AI-extracted data is allowed to affect
 * reminders or health charts, and before destructive actions.
 */
function Dialog({
  open,
  title,
  description,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  onConfirm,
  onCancel,
  danger = false
}) {
  if (!open) return null;
  return /*#__PURE__*/React.createElement("div", {
    style: {
      position: 'absolute',
      inset: 0,
      background: 'var(--surface-overlay)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 10,
      fontFamily: 'var(--font-body)'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      background: 'var(--surface-card)',
      borderRadius: 'var(--radius-lg)',
      boxShadow: 'var(--shadow-3)',
      padding: '28px',
      width: '340px',
      maxWidth: '90%'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: 'var(--font-display)',
      fontWeight: 'var(--weight-bold)',
      fontSize: 'var(--text-h3)',
      color: 'var(--text-primary)',
      marginBottom: '10px'
    }
  }, title), /*#__PURE__*/React.createElement("div", {
    style: {
      fontSize: 'var(--text-body)',
      color: 'var(--text-secondary)',
      lineHeight: 'var(--leading-normal)',
      marginBottom: '24px'
    }
  }, description), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: '10px',
      flexDirection: 'column'
    }
  }, /*#__PURE__*/React.createElement("button", {
    onClick: onConfirm,
    style: {
      height: '52px',
      borderRadius: 'var(--radius-md)',
      border: 'none',
      cursor: 'pointer',
      background: danger ? 'var(--red-800)' : 'var(--color-primary)',
      color: 'var(--text-on-primary)',
      fontFamily: 'var(--font-display)',
      fontWeight: 'var(--weight-semibold)',
      fontSize: 'var(--text-body)'
    }
  }, confirmLabel), /*#__PURE__*/React.createElement("button", {
    onClick: onCancel,
    style: {
      height: '52px',
      borderRadius: 'var(--radius-md)',
      border: 'none',
      cursor: 'pointer',
      background: 'transparent',
      color: 'var(--text-secondary)',
      fontFamily: 'var(--font-display)',
      fontWeight: 'var(--weight-semibold)',
      fontSize: 'var(--text-body)'
    }
  }, cancelLabel))));
}
Object.assign(__ds_scope, { Dialog });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/Dialog.jsx", error: String((e && e.message) || e) }); }

// components/feedback/Snackbar.jsx
try { (() => {
/** Snackbar — brief bottom confirmation message (e.g. "Medicine marked as taken"). Auto-dismisses. */
function Snackbar({
  open,
  message,
  actionLabel,
  onAction
}) {
  if (!open) return null;
  return /*#__PURE__*/React.createElement("div", {
    style: {
      position: 'absolute',
      left: '20px',
      right: '20px',
      bottom: '20px',
      background: 'var(--warm-900)',
      color: 'var(--warm-50)',
      borderRadius: 'var(--radius-md)',
      padding: '16px 20px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      gap: '12px',
      boxShadow: 'var(--shadow-3)',
      fontFamily: 'var(--font-body)',
      fontSize: 'var(--text-body)'
    }
  }, /*#__PURE__*/React.createElement("span", null, message), actionLabel ? /*#__PURE__*/React.createElement("button", {
    onClick: onAction,
    style: {
      background: 'none',
      border: 'none',
      color: 'var(--red-300)',
      fontWeight: 'var(--weight-semibold)',
      fontFamily: 'var(--font-display)',
      fontSize: 'var(--text-body)',
      cursor: 'pointer'
    }
  }, actionLabel) : null);
}
Object.assign(__ds_scope, { Snackbar });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/Snackbar.jsx", error: String((e && e.message) || e) }); }

// components/forms/Checkbox.jsx
try { (() => {
/** Checkbox — large tappable checkbox with label, for multi-select confirmation lists (e.g. confirming extracted medicines). */
function Checkbox({
  label,
  checked = false,
  onChange,
  disabled = false
}) {
  return /*#__PURE__*/React.createElement("label", {
    style: {
      display: 'flex',
      alignItems: 'center',
      gap: '14px',
      cursor: disabled ? 'not-allowed' : 'pointer',
      fontFamily: 'var(--font-body)',
      fontSize: 'var(--text-body-lg)',
      color: 'var(--text-primary)',
      opacity: disabled ? 0.5 : 1,
      minHeight: 'var(--tap-target-min)'
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      width: '28px',
      height: '28px',
      borderRadius: '8px',
      flexShrink: 0,
      border: `2px solid ${checked ? 'var(--color-primary)' : 'var(--border-default)'}`,
      background: checked ? 'var(--color-primary)' : 'var(--surface-card)',
      display: 'inline-flex',
      alignItems: 'center',
      justifyContent: 'center',
      transition: 'background var(--duration-fast) var(--ease-standard)'
    }
  }, checked ? /*#__PURE__*/React.createElement("span", {
    className: "material-symbols-outlined",
    style: {
      fontSize: '20px',
      color: 'var(--text-on-primary)'
    }
  }, "check") : null), /*#__PURE__*/React.createElement("input", {
    type: "checkbox",
    checked: checked,
    onChange: onChange,
    disabled: disabled,
    style: {
      display: 'none'
    }
  }), label);
}
Object.assign(__ds_scope, { Checkbox });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/Checkbox.jsx", error: String((e && e.message) || e) }); }

// components/forms/RadioGroup.jsx
try { (() => {
/** RadioGroup — single-choice list, each option rendered as a full-width tappable row (not a tiny radio dot). */
function RadioGroup({
  name,
  options = [],
  value,
  onChange
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      flexDirection: 'column',
      gap: '10px',
      fontFamily: 'var(--font-body)'
    },
    role: "radiogroup"
  }, options.map(o => {
    const selected = value === o.value;
    return /*#__PURE__*/React.createElement("label", {
      key: o.value,
      style: {
        display: 'flex',
        alignItems: 'center',
        gap: '14px',
        cursor: 'pointer',
        padding: '14px 18px',
        minHeight: 'var(--tap-target-min)',
        borderRadius: 'var(--radius-md)',
        border: `2px solid ${selected ? 'var(--color-primary)' : 'var(--border-default)'}`,
        background: selected ? 'var(--color-accent-blush)' : 'var(--surface-card)',
        transition: 'background var(--duration-fast) var(--ease-standard), border var(--duration-fast) var(--ease-standard)'
      }
    }, /*#__PURE__*/React.createElement("span", {
      style: {
        width: '24px',
        height: '24px',
        borderRadius: '50%',
        flexShrink: 0,
        border: `2px solid ${selected ? 'var(--color-primary)' : 'var(--border-default)'}`,
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'var(--surface-card)'
      }
    }, selected ? /*#__PURE__*/React.createElement("span", {
      style: {
        width: '12px',
        height: '12px',
        borderRadius: '50%',
        background: 'var(--color-primary)'
      }
    }) : null), /*#__PURE__*/React.createElement("input", {
      type: "radio",
      name: name,
      value: o.value,
      checked: selected,
      onChange: onChange,
      style: {
        display: 'none'
      }
    }), /*#__PURE__*/React.createElement("span", {
      style: {
        fontSize: 'var(--text-body-lg)',
        color: 'var(--text-primary)'
      }
    }, o.label));
  }));
}
Object.assign(__ds_scope, { RadioGroup });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/RadioGroup.jsx", error: String((e && e.message) || e) }); }

// components/forms/Select.jsx
try { (() => {
/** Select — dropdown for choosing one option from a short list (e.g. dose frequency, document type). */
function Select({
  label,
  value,
  onChange,
  options = [],
  placeholder = 'Select…'
}) {
  const [focused, setFocused] = React.useState(false);
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      flexDirection: 'column',
      gap: '8px',
      fontFamily: 'var(--font-body)',
      width: '100%'
    }
  }, label ? /*#__PURE__*/React.createElement("label", {
    style: {
      fontSize: 'var(--text-body-sm)',
      fontWeight: 'var(--weight-semibold)',
      color: 'var(--text-primary)'
    }
  }, label) : null, /*#__PURE__*/React.createElement("div", {
    style: {
      position: 'relative'
    }
  }, /*#__PURE__*/React.createElement("select", {
    value: value,
    onChange: onChange,
    onFocus: () => setFocused(true),
    onBlur: () => setFocused(false),
    style: {
      appearance: 'none',
      width: '100%',
      fontFamily: 'var(--font-body)',
      fontSize: 'var(--text-body-lg)',
      color: value ? 'var(--text-primary)' : 'var(--text-secondary)',
      background: 'var(--surface-card)',
      border: `2px solid ${focused ? 'var(--color-primary)' : 'var(--border-default)'}`,
      borderRadius: 'var(--radius-md)',
      padding: '0 44px 0 18px',
      height: 'var(--tap-target-min)',
      outline: 'none',
      boxShadow: focused ? 'var(--shadow-focus)' : 'none'
    }
  }, /*#__PURE__*/React.createElement("option", {
    value: "",
    disabled: true,
    hidden: true
  }, placeholder), options.map(o => /*#__PURE__*/React.createElement("option", {
    key: o.value,
    value: o.value
  }, o.label))), /*#__PURE__*/React.createElement("span", {
    className: "material-symbols-outlined",
    style: {
      position: 'absolute',
      right: '14px',
      top: '50%',
      transform: 'translateY(-50%)',
      color: 'var(--text-secondary)',
      pointerEvents: 'none'
    }
  }, "expand_more")));
}
Object.assign(__ds_scope, { Select });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/Select.jsx", error: String((e && e.message) || e) }); }

// components/forms/Switch.jsx
try { (() => {
/** Switch — on/off toggle for settings (e.g. reminder enabled, Health Connect sync). */
function Switch({
  label,
  checked = false,
  onChange,
  disabled = false
}) {
  return /*#__PURE__*/React.createElement("label", {
    style: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      gap: '16px',
      cursor: disabled ? 'not-allowed' : 'pointer',
      opacity: disabled ? 0.5 : 1,
      fontFamily: 'var(--font-body)',
      fontSize: 'var(--text-body-lg)',
      color: 'var(--text-primary)',
      minHeight: 'var(--tap-target-min)'
    }
  }, /*#__PURE__*/React.createElement("span", null, label), /*#__PURE__*/React.createElement("span", {
    style: {
      width: '52px',
      height: '32px',
      borderRadius: 'var(--radius-full)',
      flexShrink: 0,
      background: checked ? 'var(--color-primary)' : 'var(--warm-200)',
      position: 'relative',
      transition: 'background var(--duration-fast) var(--ease-standard)'
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      position: 'absolute',
      top: '3px',
      left: checked ? '23px' : '3px',
      width: '26px',
      height: '26px',
      borderRadius: '50%',
      background: 'var(--surface-card)',
      boxShadow: 'var(--shadow-1)',
      transition: 'left var(--duration-fast) var(--ease-standard)'
    }
  })), /*#__PURE__*/React.createElement("input", {
    type: "checkbox",
    checked: checked,
    onChange: onChange,
    disabled: disabled,
    style: {
      display: 'none'
    }
  }));
}
Object.assign(__ds_scope, { Switch });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/Switch.jsx", error: String((e && e.message) || e) }); }

// components/forms/TextField.jsx
try { (() => {
/** TextField — labeled text input with helper/error text. Large touch target and text size for accessibility. */
function TextField({
  label,
  placeholder,
  helperText,
  error,
  value,
  onChange,
  type = 'text',
  multiline = false
}) {
  const [focused, setFocused] = React.useState(false);
  const borderColor = error ? 'var(--red-700)' : focused ? 'var(--color-primary)' : 'var(--border-default)';
  const Tag = multiline ? 'textarea' : 'input';
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      flexDirection: 'column',
      gap: '8px',
      fontFamily: 'var(--font-body)',
      width: '100%'
    }
  }, label ? /*#__PURE__*/React.createElement("label", {
    style: {
      fontSize: 'var(--text-body-sm)',
      fontWeight: 'var(--weight-semibold)',
      color: 'var(--text-primary)'
    }
  }, label) : null, /*#__PURE__*/React.createElement(Tag, {
    type: multiline ? undefined : type,
    value: value,
    placeholder: placeholder,
    onChange: onChange,
    onFocus: () => setFocused(true),
    onBlur: () => setFocused(false),
    rows: multiline ? 3 : undefined,
    style: {
      fontFamily: 'var(--font-body)',
      fontSize: 'var(--text-body-lg)',
      color: 'var(--text-primary)',
      background: 'var(--surface-card)',
      border: `2px solid ${borderColor}`,
      borderRadius: 'var(--radius-md)',
      padding: multiline ? '14px 18px' : '0 18px',
      height: multiline ? 'auto' : 'var(--tap-target-min)',
      outline: 'none',
      boxShadow: focused ? 'var(--shadow-focus)' : 'none',
      transition: 'border var(--duration-fast) var(--ease-standard), box-shadow var(--duration-fast) var(--ease-standard)'
    }
  }), helperText || error ? /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: 'var(--text-body-sm)',
      color: error ? 'var(--red-700)' : 'var(--text-secondary)'
    }
  }, error || helperText) : null);
}
Object.assign(__ds_scope, { TextField });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/TextField.jsx", error: String((e && e.message) || e) }); }

// components/navigation/BottomNavBar.jsx
try { (() => {
/** BottomNavBar — Material 3 bottom navigation with up to 4 large icon+label tabs. */
function BottomNavBar({
  items = [],
  active,
  onChange
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      height: '76px',
      background: 'var(--surface-card)',
      borderTop: '1px solid var(--border-default)',
      boxShadow: 'var(--shadow-2)',
      fontFamily: 'var(--font-display)'
    }
  }, items.map(it => {
    const isActive = it.key === active;
    return /*#__PURE__*/React.createElement("button", {
      key: it.key,
      onClick: () => onChange && onChange(it.key),
      style: {
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '4px',
        border: 'none',
        background: 'transparent',
        cursor: 'pointer',
        color: isActive ? 'var(--color-primary)' : 'var(--text-secondary)'
      }
    }, /*#__PURE__*/React.createElement("span", {
      className: "material-symbols-outlined",
      style: {
        fontSize: '24px',
        fontVariationSettings: isActive ? "'FILL' 1" : "'FILL' 0"
      }
    }, it.icon), /*#__PURE__*/React.createElement("span", {
      style: {
        fontSize: '13px',
        fontWeight: isActive ? 'var(--weight-semibold)' : 'var(--weight-medium)'
      }
    }, it.label));
  }));
}
Object.assign(__ds_scope, { BottomNavBar });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/navigation/BottomNavBar.jsx", error: String((e && e.message) || e) }); }

// components/navigation/TopAppBar.jsx
try { (() => {
/** TopAppBar — Material 3-style top bar with optional back button, title, and one trailing action. */
function TopAppBar({
  title,
  onBack,
  action
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center',
      gap: '8px',
      height: '64px',
      padding: '0 8px',
      background: 'var(--surface-bg)',
      fontFamily: 'var(--font-display)'
    }
  }, onBack ? /*#__PURE__*/React.createElement("button", {
    onClick: onBack,
    "aria-label": "Back",
    style: {
      width: '48px',
      height: '48px',
      borderRadius: 'var(--radius-full)',
      border: 'none',
      background: 'transparent',
      display: 'inline-flex',
      alignItems: 'center',
      justifyContent: 'center',
      cursor: 'pointer'
    }
  }, /*#__PURE__*/React.createElement("span", {
    className: "material-symbols-outlined",
    style: {
      fontSize: '24px',
      color: 'var(--text-primary)'
    }
  }, "arrow_back")) : /*#__PURE__*/React.createElement("span", {
    style: {
      width: '8px'
    }
  }), /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: 'var(--text-h3)',
      fontWeight: 'var(--weight-bold)',
      color: 'var(--text-primary)',
      flex: 1
    }
  }, title), action ? /*#__PURE__*/React.createElement("button", {
    onClick: action.onClick,
    "aria-label": action.label,
    style: {
      width: '48px',
      height: '48px',
      borderRadius: 'var(--radius-full)',
      border: 'none',
      background: 'transparent',
      display: 'inline-flex',
      alignItems: 'center',
      justifyContent: 'center',
      cursor: 'pointer'
    }
  }, /*#__PURE__*/React.createElement("span", {
    className: "material-symbols-outlined",
    style: {
      fontSize: '24px',
      color: 'var(--text-primary)'
    }
  }, action.icon)) : null);
}
Object.assign(__ds_scope, { TopAppBar });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/navigation/TopAppBar.jsx", error: String((e && e.message) || e) }); }

// components/surfaces/Card.jsx
try { (() => {
/** Card — the base surface for medicine rows, vital entries, insight tiles. Soft warm shadow, generous rounding. */
function Card({
  children,
  padding = 20,
  interactive = false,
  onClick
}) {
  const [hover, setHover] = React.useState(false);
  return /*#__PURE__*/React.createElement("div", {
    onClick: onClick,
    onMouseEnter: () => interactive && setHover(true),
    onMouseLeave: () => setHover(false),
    style: {
      background: 'var(--surface-card)',
      border: '1px solid var(--border-default)',
      borderRadius: 'var(--radius-lg)',
      padding: `${padding}px`,
      boxShadow: hover ? 'var(--shadow-2)' : 'var(--shadow-1)',
      cursor: interactive ? 'pointer' : 'default',
      transition: 'box-shadow var(--duration-fast) var(--ease-standard), transform var(--duration-fast) var(--ease-standard)',
      transform: hover ? 'translateY(-2px)' : 'none',
      fontFamily: 'var(--font-body)'
    }
  }, children);
}
Object.assign(__ds_scope, { Card });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/surfaces/Card.jsx", error: String((e && e.message) || e) }); }

// ui_kits/android_app/HomeScreen.jsx
try { (() => {
// HomeScreen — dashboard: greeting, today's medicines, today's vitals, upload CTA.
function HomeScreen({
  onOpenMedicine,
  onUpload,
  onMarkTaken,
  medicines,
  vitals
}) {
  const {
    Card,
    Badge,
    ProvenanceTag,
    Button,
    IconButton
  } = window.MediHelpDesignSystem_235ce9;
  return /*#__PURE__*/React.createElement("div", {
    style: {
      padding: '16px',
      display: 'flex',
      flexDirection: 'column',
      gap: '16px',
      fontFamily: 'var(--font-body)',
      paddingBottom: '90px'
    }
  }, /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: 'var(--font-display)',
      fontWeight: 800,
      fontSize: 'var(--text-h1)',
      color: 'var(--text-primary)'
    }
  }, "Good morning, Fatima"), /*#__PURE__*/React.createElement("div", {
    style: {
      color: 'var(--text-secondary)',
      fontSize: 'var(--text-body)'
    }
  }, "Here's how you're doing today.")), /*#__PURE__*/React.createElement(Button, {
    variant: "primary",
    icon: "upload_file",
    fullWidth: true,
    onClick: onUpload
  }, "Upload Prescription or Lab Report"), /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: 'var(--font-display)',
      fontWeight: 700,
      fontSize: 'var(--text-h2)',
      color: 'var(--text-primary)'
    }
  }, "Today's Medicines"), medicines.map(m => /*#__PURE__*/React.createElement(Card, {
    key: m.id,
    interactive: true,
    onClick: () => onOpenMedicine(m)
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'flex-start',
      gap: '10px'
    }
  }, /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: 'var(--font-display)',
      fontWeight: 700,
      fontSize: 'var(--text-h3)',
      color: 'var(--text-primary)'
    }
  }, m.name), /*#__PURE__*/React.createElement("div", {
    style: {
      color: 'var(--text-secondary)',
      marginTop: '4px'
    }
  }, m.time)), /*#__PURE__*/React.createElement(IconButton, {
    icon: m.taken ? 'check_circle' : 'radio_button_unchecked',
    label: m.taken ? 'Taken' : 'Mark as taken',
    variant: m.taken ? 'filled' : 'ghost',
    onClick: e => {
      e.stopPropagation();
      onMarkTaken(m.id);
    }
  })))), /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: 'var(--font-display)',
      fontWeight: 700,
      fontSize: 'var(--text-h2)',
      color: 'var(--text-primary)',
      marginTop: '8px'
    }
  }, "Today's Vitals"), vitals.map(v => /*#__PURE__*/React.createElement(Card, {
    key: v.id
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center'
    }
  }, /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: 'var(--font-display)',
      fontWeight: 700,
      fontSize: 'var(--text-h3)',
      color: 'var(--text-primary)'
    }
  }, v.value, " ", /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: 'var(--text-body-sm)',
      color: 'var(--text-secondary)',
      fontWeight: 500
    }
  }, v.unit)), /*#__PURE__*/React.createElement("div", {
    style: {
      marginTop: '4px'
    }
  }, /*#__PURE__*/React.createElement(ProvenanceTag, {
    date: v.date,
    source: v.source
  }))), /*#__PURE__*/React.createElement(Badge, {
    status: v.status
  })))));
}
window.HomeScreen = HomeScreen;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/android_app/HomeScreen.jsx", error: String((e && e.message) || e) }); }

// ui_kits/android_app/InsightsScreen.jsx
try { (() => {
// InsightsScreen — non-diagnostic health tips & trend summaries.
function InsightsScreen({
  insights
}) {
  const {
    Card
  } = window.MediHelpDesignSystem_235ce9;
  return /*#__PURE__*/React.createElement("div", {
    style: {
      padding: '16px',
      display: 'flex',
      flexDirection: 'column',
      gap: '14px',
      fontFamily: 'var(--font-body)',
      paddingBottom: '90px'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: 'var(--font-display)',
      fontWeight: 800,
      fontSize: 'var(--text-h1)',
      color: 'var(--text-primary)'
    }
  }, "Your Health Tips"), /*#__PURE__*/React.createElement("div", {
    style: {
      color: 'var(--text-secondary)'
    }
  }, "Generated Jul 9, 2026 \xB7 based on the last 30 days"), insights.map(ins => /*#__PURE__*/React.createElement(Card, {
    key: ins.id
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: '12px',
      alignItems: 'flex-start'
    }
  }, /*#__PURE__*/React.createElement("span", {
    className: "material-symbols-outlined",
    style: {
      fontSize: '26px',
      color: 'var(--color-primary)'
    }
  }, ins.icon), /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: 'var(--font-display)',
      fontWeight: 700,
      fontSize: 'var(--text-h3)',
      color: 'var(--text-primary)'
    }
  }, ins.title), /*#__PURE__*/React.createElement("div", {
    style: {
      color: 'var(--text-primary)',
      marginTop: '6px',
      lineHeight: 'var(--leading-loose)'
    }
  }, ins.body))))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: '8px',
      alignItems: 'flex-start',
      color: 'var(--text-secondary)',
      fontSize: 'var(--text-body-sm)',
      marginTop: '4px'
    }
  }, /*#__PURE__*/React.createElement("span", {
    className: "material-symbols-outlined",
    style: {
      fontSize: '18px'
    }
  }, "info"), /*#__PURE__*/React.createElement("span", null, "These tips are general suggestions, not a medical diagnosis. Always check with your doctor before changing your care.")));
}
window.InsightsScreen = InsightsScreen;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/android_app/InsightsScreen.jsx", error: String((e && e.message) || e) }); }

// ui_kits/android_app/MedicinesScreen.jsx
try { (() => {
// MedicinesScreen — full medicine list + detail overlay with simplified explanation.
function MedicinesScreen({
  medicines,
  selected,
  onSelect,
  onClose
}) {
  const {
    Card,
    Badge
  } = window.MediHelpDesignSystem_235ce9;
  if (selected) {
    return /*#__PURE__*/React.createElement("div", {
      style: {
        padding: '16px',
        fontFamily: 'var(--font-body)',
        paddingBottom: '90px'
      }
    }, /*#__PURE__*/React.createElement("button", {
      onClick: onClose,
      style: {
        background: 'none',
        border: 'none',
        color: 'var(--color-primary)',
        fontFamily: 'var(--font-display)',
        fontWeight: 600,
        fontSize: 'var(--text-body)',
        cursor: 'pointer',
        padding: 0,
        marginBottom: '16px'
      }
    }, "\u2190 Back to Medicines"), /*#__PURE__*/React.createElement("div", {
      style: {
        fontFamily: 'var(--font-display)',
        fontWeight: 800,
        fontSize: 'var(--text-h1)',
        color: 'var(--text-primary)'
      }
    }, selected.name), /*#__PURE__*/React.createElement("div", {
      style: {
        color: 'var(--text-secondary)',
        marginTop: '6px',
        fontSize: 'var(--text-body)'
      }
    }, selected.time), /*#__PURE__*/React.createElement("div", {
      style: {
        marginTop: '20px',
        background: 'var(--color-accent-blush)',
        borderRadius: 'var(--radius-lg)',
        padding: '18px'
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        fontFamily: 'var(--font-display)',
        fontWeight: 700,
        color: 'var(--red-800)',
        marginBottom: '8px'
      }
    }, "What it's for"), /*#__PURE__*/React.createElement("div", {
      style: {
        fontSize: 'var(--text-body-lg)',
        lineHeight: 'var(--leading-loose)',
        color: 'var(--text-primary)'
      }
    }, selected.purpose)), /*#__PURE__*/React.createElement("div", {
      style: {
        marginTop: '14px',
        background: 'var(--surface-card)',
        border: '1px solid var(--border-default)',
        borderRadius: 'var(--radius-lg)',
        padding: '18px'
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        fontFamily: 'var(--font-display)',
        fontWeight: 700,
        color: 'var(--text-primary)',
        marginBottom: '8px'
      }
    }, "How to take it"), /*#__PURE__*/React.createElement("div", {
      style: {
        fontSize: 'var(--text-body-lg)',
        lineHeight: 'var(--leading-loose)',
        color: 'var(--text-primary)'
      }
    }, selected.instructions)), /*#__PURE__*/React.createElement("div", {
      style: {
        marginTop: '14px',
        display: 'flex',
        gap: '8px',
        alignItems: 'flex-start',
        color: 'var(--text-secondary)',
        fontSize: 'var(--text-body-sm)'
      }
    }, /*#__PURE__*/React.createElement("span", {
      className: "material-symbols-outlined",
      style: {
        fontSize: '18px'
      }
    }, "info"), /*#__PURE__*/React.createElement("span", null, "This explanation is simplified for easy reading and is not a substitute for medical advice. Ask your doctor if anything is unclear.")));
  }
  return /*#__PURE__*/React.createElement("div", {
    style: {
      padding: '16px',
      display: 'flex',
      flexDirection: 'column',
      gap: '12px',
      fontFamily: 'var(--font-body)',
      paddingBottom: '90px'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: 'var(--font-display)',
      fontWeight: 800,
      fontSize: 'var(--text-h1)',
      color: 'var(--text-primary)'
    }
  }, "My Medicines"), medicines.map(m => /*#__PURE__*/React.createElement(Card, {
    key: m.id,
    interactive: true,
    onClick: () => onSelect(m)
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center'
    }
  }, /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: 'var(--font-display)',
      fontWeight: 700,
      fontSize: 'var(--text-h3)',
      color: 'var(--text-primary)'
    }
  }, m.name), /*#__PURE__*/React.createElement("div", {
    style: {
      color: 'var(--text-secondary)',
      marginTop: '4px'
    }
  }, m.time)), /*#__PURE__*/React.createElement(Badge, {
    status: "info"
  }, m.status)))));
}
window.MedicinesScreen = MedicinesScreen;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/android_app/MedicinesScreen.jsx", error: String((e && e.message) || e) }); }

// ui_kits/android_app/SettingsScreen.jsx
try { (() => {
// SettingsScreen — accessibility & sync toggles.
function SettingsScreen({
  onBack,
  prefs,
  onTogglePref
}) {
  const {
    Switch
  } = window.MediHelpDesignSystem_235ce9;
  return /*#__PURE__*/React.createElement("div", {
    style: {
      padding: '16px',
      fontFamily: 'var(--font-body)',
      display: 'flex',
      flexDirection: 'column',
      gap: '4px'
    }
  }, /*#__PURE__*/React.createElement("button", {
    onClick: onBack,
    style: {
      background: 'none',
      border: 'none',
      color: 'var(--color-primary)',
      fontFamily: 'var(--font-display)',
      fontWeight: 600,
      cursor: 'pointer',
      padding: 0,
      marginBottom: '12px',
      alignSelf: 'flex-start'
    }
  }, "\u2190 Back"), /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: 'var(--font-display)',
      fontWeight: 800,
      fontSize: 'var(--text-h1)',
      color: 'var(--text-primary)',
      marginBottom: '10px'
    }
  }, "Settings"), /*#__PURE__*/React.createElement(Switch, {
    label: "Medication reminders",
    checked: prefs.reminders,
    onChange: () => onTogglePref('reminders')
  }), /*#__PURE__*/React.createElement(Switch, {
    label: "Sync with Health Connect",
    checked: prefs.healthConnect,
    onChange: () => onTogglePref('healthConnect')
  }), /*#__PURE__*/React.createElement(Switch, {
    label: "Large text mode",
    checked: prefs.largeText,
    onChange: () => onTogglePref('largeText')
  }), /*#__PURE__*/React.createElement(Switch, {
    label: "High-contrast mode",
    checked: prefs.highContrast,
    onChange: () => onTogglePref('highContrast')
  }));
}
window.SettingsScreen = SettingsScreen;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/android_app/SettingsScreen.jsx", error: String((e && e.message) || e) }); }

// ui_kits/android_app/UploadReviewScreen.jsx
try { (() => {
// UploadReviewScreen — upload doc type selection, then AI-extracted review with confirm dialog.
function UploadReviewScreen({
  step,
  onPickType,
  onProcessed,
  onConfirm,
  onBack,
  selections,
  onToggleSelection,
  showDialog,
  onDialogConfirm,
  onDialogCancel
}) {
  const {
    RadioGroup,
    Button,
    Checkbox,
    Dialog,
    Badge
  } = window.MediHelpDesignSystem_235ce9;
  if (step === 'type') {
    return /*#__PURE__*/React.createElement("div", {
      style: {
        padding: '16px',
        fontFamily: 'var(--font-body)',
        display: 'flex',
        flexDirection: 'column',
        gap: '18px'
      }
    }, /*#__PURE__*/React.createElement("button", {
      onClick: onBack,
      style: {
        background: 'none',
        border: 'none',
        color: 'var(--color-primary)',
        fontFamily: 'var(--font-display)',
        fontWeight: 600,
        cursor: 'pointer',
        padding: 0,
        alignSelf: 'flex-start'
      }
    }, "\u2190 Cancel"), /*#__PURE__*/React.createElement("div", {
      style: {
        fontFamily: 'var(--font-display)',
        fontWeight: 800,
        fontSize: 'var(--text-h1)',
        color: 'var(--text-primary)'
      }
    }, "What are you uploading?"), /*#__PURE__*/React.createElement(RadioGroup, {
      name: "doctype",
      value: undefined,
      options: [{
        value: 'prescription',
        label: 'A Prescription'
      }, {
        value: 'lab',
        label: 'A Lab Report'
      }, {
        value: 'unsure',
        label: "I'm not sure"
      }],
      onChange: onPickType
    }), /*#__PURE__*/React.createElement("div", {
      style: {
        display: 'flex',
        flexDirection: 'column',
        gap: '12px',
        alignItems: 'center',
        border: '2px dashed var(--border-strong)',
        borderRadius: 'var(--radius-lg)',
        padding: '28px',
        color: 'var(--text-secondary)'
      }
    }, /*#__PURE__*/React.createElement("span", {
      className: "material-symbols-outlined",
      style: {
        fontSize: '36px',
        color: 'var(--color-primary)'
      }
    }, "camera_alt"), /*#__PURE__*/React.createElement("span", null, "Take a photo or choose a file")), /*#__PURE__*/React.createElement(Button, {
      variant: "primary",
      fullWidth: true,
      onClick: onProcessed
    }, "Continue"));
  }
  if (step === 'processing') {
    return /*#__PURE__*/React.createElement("div", {
      style: {
        padding: '16px',
        fontFamily: 'var(--font-body)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        height: '500px',
        gap: '16px',
        textAlign: 'center'
      }
    }, /*#__PURE__*/React.createElement("span", {
      className: "material-symbols-outlined",
      style: {
        fontSize: '48px',
        color: 'var(--color-primary)'
      }
    }, "hourglass_top"), /*#__PURE__*/React.createElement("div", {
      style: {
        fontFamily: 'var(--font-display)',
        fontWeight: 700,
        fontSize: 'var(--text-h3)',
        color: 'var(--text-primary)'
      }
    }, "Reading your document\u2026"), /*#__PURE__*/React.createElement("div", {
      style: {
        color: 'var(--text-secondary)'
      }
    }, "This usually takes less than a minute."), /*#__PURE__*/React.createElement(Button, {
      variant: "secondary",
      onClick: onConfirm
    }, "Skip to Review (demo)"));
  }

  // step === 'review'
  const items = [{
    id: 1,
    name: 'Metformin 500mg',
    detail: 'Twice daily, after food'
  }, {
    id: 2,
    name: 'Amlodipine 5mg',
    detail: 'Once daily, morning'
  }];
  return /*#__PURE__*/React.createElement("div", {
    style: {
      padding: '16px',
      fontFamily: 'var(--font-body)',
      display: 'flex',
      flexDirection: 'column',
      gap: '16px',
      position: 'relative'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: 'var(--font-display)',
      fontWeight: 800,
      fontSize: 'var(--text-h1)',
      color: 'var(--text-primary)'
    }
  }, "We found 2 medicines"), /*#__PURE__*/React.createElement("div", {
    style: {
      color: 'var(--text-secondary)'
    }
  }, "Review and uncheck anything that's not right."), items.map(it => /*#__PURE__*/React.createElement("div", {
    key: it.id,
    style: {
      border: '1px solid var(--border-default)',
      borderRadius: 'var(--radius-lg)',
      padding: '14px 16px',
      background: 'var(--surface-card)'
    }
  }, /*#__PURE__*/React.createElement(Checkbox, {
    label: `${it.name} — ${it.detail}`,
    checked: selections[it.id] !== false,
    onChange: () => onToggleSelection(it.id)
  }))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: '8px',
      alignItems: 'flex-start',
      color: 'var(--text-secondary)',
      fontSize: 'var(--text-body-sm)'
    }
  }, /*#__PURE__*/React.createElement("span", {
    className: "material-symbols-outlined",
    style: {
      fontSize: '18px'
    }
  }, "shield"), /*#__PURE__*/React.createElement("span", null, "Nothing is added to your reminders until you confirm below.")), /*#__PURE__*/React.createElement(Button, {
    variant: "primary",
    fullWidth: true,
    onClick: onDialogConfirm
  }, "Confirm & Add to My Medicines"), showDialog ? /*#__PURE__*/React.createElement(Dialog, {
    open: true,
    title: "Add these medicines?",
    description: "Adding them will create reminders for Metformin and Amlodipine on your Medicine Dashboard.",
    onConfirm: onConfirm,
    onCancel: onDialogCancel
  }) : null);
}
window.UploadReviewScreen = UploadReviewScreen;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/android_app/UploadReviewScreen.jsx", error: String((e && e.message) || e) }); }

// ui_kits/android_app/VitalsScreen.jsx
try { (() => {
// VitalsScreen — health chart list with provenance + add-vital form.
function VitalsScreen({
  vitals,
  showAdd,
  onOpenAdd,
  onCloseAdd
}) {
  const {
    Card,
    Badge,
    ProvenanceTag,
    TextField,
    Select,
    Button
  } = window.MediHelpDesignSystem_235ce9;
  const [metric, setMetric] = React.useState('');
  const [val, setVal] = React.useState('');
  if (showAdd) {
    return /*#__PURE__*/React.createElement("div", {
      style: {
        padding: '16px',
        fontFamily: 'var(--font-body)',
        paddingBottom: '90px',
        display: 'flex',
        flexDirection: 'column',
        gap: '16px'
      }
    }, /*#__PURE__*/React.createElement("div", {
      style: {
        fontFamily: 'var(--font-display)',
        fontWeight: 800,
        fontSize: 'var(--text-h1)',
        color: 'var(--text-primary)'
      }
    }, "Add a Vital"), /*#__PURE__*/React.createElement(Select, {
      label: "What are you logging?",
      value: metric,
      onChange: e => setMetric(e.target.value),
      options: [{
        value: 'bp',
        label: 'Blood Pressure'
      }, {
        value: 'hr',
        label: 'Heart Rate'
      }, {
        value: 'glucose',
        label: 'Blood Glucose'
      }]
    }), /*#__PURE__*/React.createElement(TextField, {
      label: "Value",
      placeholder: "e.g. 120/80",
      value: val,
      onChange: e => setVal(e.target.value),
      helperText: "We'll save today's date and mark this as Manual entry."
    }), /*#__PURE__*/React.createElement(Button, {
      variant: "primary",
      fullWidth: true,
      onClick: onCloseAdd
    }, "Save Vital"), /*#__PURE__*/React.createElement(Button, {
      variant: "ghost",
      fullWidth: true,
      onClick: onCloseAdd
    }, "Cancel"));
  }
  return /*#__PURE__*/React.createElement("div", {
    style: {
      padding: '16px',
      display: 'flex',
      flexDirection: 'column',
      gap: '12px',
      fontFamily: 'var(--font-body)',
      paddingBottom: '90px'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: 'var(--font-display)',
      fontWeight: 800,
      fontSize: 'var(--text-h1)',
      color: 'var(--text-primary)'
    }
  }, "Health Chart")), /*#__PURE__*/React.createElement(Button, {
    variant: "secondary",
    icon: "add",
    onClick: onOpenAdd
  }, "Add Vital Manually"), vitals.map(v => /*#__PURE__*/React.createElement(Card, {
    key: v.id
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center'
    }
  }, /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("div", {
    style: {
      color: 'var(--text-secondary)',
      fontSize: 'var(--text-body-sm)',
      fontWeight: 600,
      textTransform: 'uppercase',
      letterSpacing: 'var(--tracking-label)'
    }
  }, v.label), /*#__PURE__*/React.createElement("div", {
    style: {
      fontFamily: 'var(--font-display)',
      fontWeight: 700,
      fontSize: 'var(--text-h2)',
      color: 'var(--text-primary)',
      marginTop: '2px'
    }
  }, v.value, " ", /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: 'var(--text-body-sm)',
      color: 'var(--text-secondary)',
      fontWeight: 500
    }
  }, v.unit)), /*#__PURE__*/React.createElement("div", {
    style: {
      marginTop: '6px'
    }
  }, /*#__PURE__*/React.createElement(ProvenanceTag, {
    date: v.date,
    source: v.source
  }))), /*#__PURE__*/React.createElement(Badge, {
    status: v.status
  })))));
}
window.VitalsScreen = VitalsScreen;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/android_app/VitalsScreen.jsx", error: String((e && e.message) || e) }); }

// ui_kits/android_app/android-frame.jsx
try { (() => {
// @ds-adherence-ignore -- omelette starter scaffold (raw elements/hex/px by design)

/* BEGIN USAGE */
// Android.jsx — Simplified Android (Material 3) device frame
// Status bar + top app bar + content + gesture nav + keyboard.
// Based on Figma M3 spec. No dependencies, no image assets.
// Exports (to window): AndroidDevice, AndroidStatusBar, AndroidAppBar, AndroidListItem, AndroidNavBar, AndroidKeyboard
//
// Usage — wrap your screen content in <AndroidDevice> to get the bezel, status
// bar and gesture nav (props: title, large, keyboard, dark):
//
//   <AndroidDevice title="Inbox" large>
//     ...your screen content...
//   </AndroidDevice>
//   <AndroidDevice title="Compose" keyboard>…</AndroidDevice>
/* END USAGE */

const MD_C = {
  surface: '#f4fbf8',
  surfaceVariant: '#dae5e1',
  inverseOnSurface: '#ecf2ef',
  secondaryContainer: '#cde8e1',
  primaryFixedDim: '#83d5c6',
  onSurface: '#171d1b',
  onSurfaceVar: '#49454f',
  onPrimaryContainer: '#00201c',
  primary: '#006a60',
  frameBorder: 'rgba(116,119,117,0.5)'
};

// ─────────────────────────────────────────────────────────────
// Status bar (time left, wifi/cell/battery right)
// ─────────────────────────────────────────────────────────────
function AndroidStatusBar({
  dark = false
}) {
  const c = dark ? '#fff' : MD_C.onSurface;
  return /*#__PURE__*/React.createElement("div", {
    style: {
      height: 40,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '0 16px',
      position: 'relative',
      fontFamily: 'Roboto, system-ui, sans-serif'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      width: 128,
      display: 'flex',
      alignItems: 'center',
      gap: 8
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: 14,
      fontWeight: 400,
      letterSpacing: 0.25,
      lineHeight: '20px',
      color: c
    }
  }, "9:30")), /*#__PURE__*/React.createElement("div", {
    style: {
      position: 'absolute',
      left: '50%',
      top: 8,
      transform: 'translateX(-50%)',
      width: 24,
      height: 24,
      borderRadius: 100,
      background: '#2e2e2e'
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      paddingRight: 2
    }
  }, /*#__PURE__*/React.createElement("svg", {
    width: "16",
    height: "16",
    viewBox: "0 0 16 16",
    style: {
      marginRight: -2
    }
  }, /*#__PURE__*/React.createElement("path", {
    d: "M8 13.3L.67 5.97a10.37 10.37 0 0114.66 0L8 13.3z",
    fill: c
  })), /*#__PURE__*/React.createElement("svg", {
    width: "16",
    height: "16",
    viewBox: "0 0 16 16",
    style: {
      marginRight: -2
    }
  }, /*#__PURE__*/React.createElement("path", {
    d: "M14.67 14.67V1.33L1.33 14.67h13.34z",
    fill: c
  }))), /*#__PURE__*/React.createElement("svg", {
    width: "16",
    height: "16",
    viewBox: "0 0 16 16"
  }, /*#__PURE__*/React.createElement("rect", {
    x: "3.75",
    y: "2",
    width: "8.5",
    height: "13",
    rx: "1.5",
    fill: c
  }), /*#__PURE__*/React.createElement("rect", {
    x: "5.5",
    y: "0.9",
    width: "5",
    height: "2",
    rx: "0.5",
    fill: c
  }))));
}

// ─────────────────────────────────────────────────────────────
// Top app bar (Material 3 small/medium)
// ─────────────────────────────────────────────────────────────
function AndroidAppBar({
  title = 'Title',
  large = false
}) {
  const iconDot = /*#__PURE__*/React.createElement("div", {
    style: {
      width: 48,
      height: 48,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      width: 22,
      height: 22,
      borderRadius: '50%',
      background: MD_C.onSurfaceVar,
      opacity: 0.3
    }
  }));
  return /*#__PURE__*/React.createElement("div", {
    style: {
      background: MD_C.surface,
      padding: '4px 4px 0'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      height: 56,
      display: 'flex',
      alignItems: 'center',
      gap: 4
    }
  }, iconDot, !large && /*#__PURE__*/React.createElement("span", {
    style: {
      flex: 1,
      fontSize: 22,
      fontWeight: 400,
      color: MD_C.onSurface,
      fontFamily: 'Roboto, system-ui, sans-serif'
    }
  }, title), large && /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1
    }
  }), iconDot), large && /*#__PURE__*/React.createElement("div", {
    style: {
      padding: '16px 16px 20px',
      fontSize: 28,
      fontWeight: 400,
      color: MD_C.onSurface,
      fontFamily: 'Roboto, system-ui, sans-serif'
    }
  }, title));
}

// ─────────────────────────────────────────────────────────────
// List item (Material 3)
// ─────────────────────────────────────────────────────────────
function AndroidListItem({
  headline,
  supporting,
  leading
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center',
      gap: 16,
      padding: '12px 16px',
      minHeight: 56,
      boxSizing: 'border-box',
      fontFamily: 'Roboto, system-ui, sans-serif'
    }
  }, leading && /*#__PURE__*/React.createElement("div", {
    style: {
      width: 40,
      height: 40,
      borderRadius: '50%',
      background: MD_C.primary,
      color: '#fff',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      fontSize: 18,
      fontWeight: 500,
      flexShrink: 0
    }
  }, leading), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontSize: 16,
      color: MD_C.onSurface,
      lineHeight: '24px'
    }
  }, headline), supporting && /*#__PURE__*/React.createElement("div", {
    style: {
      fontSize: 14,
      color: MD_C.onSurfaceVar,
      lineHeight: '20px'
    }
  }, supporting)));
}

// ─────────────────────────────────────────────────────────────
// Gesture nav bar (pill)
// ─────────────────────────────────────────────────────────────
function AndroidNavBar({
  dark = false
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      height: 24,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      width: 108,
      height: 4,
      borderRadius: 2,
      background: dark ? '#fff' : MD_C.onSurface,
      opacity: 0.4
    }
  }));
}

// ─────────────────────────────────────────────────────────────
// Device frame — wraps everything
// ─────────────────────────────────────────────────────────────
function AndroidDevice({
  children,
  width = 412,
  height = 892,
  dark = false,
  title,
  large = false,
  keyboard = false
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      width,
      height,
      borderRadius: 18,
      overflow: 'hidden',
      background: dark ? '#1d1b20' : MD_C.surface,
      border: `8px solid ${MD_C.frameBorder}`,
      boxShadow: '0 30px 80px rgba(0,0,0,0.25)',
      display: 'flex',
      flexDirection: 'column',
      boxSizing: 'border-box'
    }
  }, /*#__PURE__*/React.createElement(AndroidStatusBar, {
    dark: dark
  }), title !== undefined && /*#__PURE__*/React.createElement(AndroidAppBar, {
    title: title,
    large: large
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      overflow: 'auto'
    }
  }, children), keyboard && /*#__PURE__*/React.createElement(AndroidKeyboard, null), /*#__PURE__*/React.createElement(AndroidNavBar, {
    dark: dark
  }));
}

// ─────────────────────────────────────────────────────────────
// Keyboard — Gboard (Material 3)
// ─────────────────────────────────────────────────────────────
function AndroidKeyboard() {
  let _k = 0;
  const key = (l, {
    flex = 1,
    bg = MD_C.surface,
    r = 6,
    minW,
    fs = 21
  } = {}) => /*#__PURE__*/React.createElement("div", {
    key: _k++,
    style: {
      height: 46,
      borderRadius: r,
      flex,
      minWidth: minW,
      background: bg,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      fontFamily: 'Roboto, system-ui',
      fontSize: fs,
      color: MD_C.onPrimaryContainer
    }
  }, l);
  const row = (keys, style = {}) => /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: 6,
      justifyContent: 'center',
      ...style
    }
  }, keys.map(l => key(l)));
  return /*#__PURE__*/React.createElement("div", {
    style: {
      background: MD_C.inverseOnSurface,
      padding: '0 8px 8px',
      display: 'flex',
      flexDirection: 'column',
      gap: 4
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      height: 44
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      flexDirection: 'column',
      gap: 12
    }
  }, row(['q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p']), row(['a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l'], {
    padding: '0 20px'
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: 6
    }
  }, key('', {
    bg: MD_C.surfaceVariant
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: 6,
      flex: 7,
      minWidth: 274
    }
  }, ['z', 'x', 'c', 'v', 'b', 'n', 'm'].map(l => key(l))), key('', {
    bg: MD_C.surfaceVariant
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: 6
    }
  }, key('?123', {
    bg: MD_C.secondaryContainer,
    r: 100,
    minW: 58,
    fs: 14
  }), key(',', {
    bg: MD_C.surfaceVariant
  }), key('', {
    flex: 3,
    minW: 154
  }), key('.', {
    bg: MD_C.surfaceVariant
  }), key('', {
    bg: MD_C.primaryFixedDim,
    r: 100,
    minW: 58
  }))));
}
Object.assign(window, {
  AndroidDevice,
  AndroidStatusBar,
  AndroidAppBar,
  AndroidListItem,
  AndroidNavBar,
  AndroidKeyboard
});
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/android_app/android-frame.jsx", error: String((e && e.message) || e) }); }

__ds_ns.Button = __ds_scope.Button;

__ds_ns.IconButton = __ds_scope.IconButton;

__ds_ns.ProvenanceTag = __ds_scope.ProvenanceTag;

__ds_ns.Badge = __ds_scope.Badge;

__ds_ns.Dialog = __ds_scope.Dialog;

__ds_ns.Snackbar = __ds_scope.Snackbar;

__ds_ns.Checkbox = __ds_scope.Checkbox;

__ds_ns.RadioGroup = __ds_scope.RadioGroup;

__ds_ns.Select = __ds_scope.Select;

__ds_ns.Switch = __ds_scope.Switch;

__ds_ns.TextField = __ds_scope.TextField;

__ds_ns.BottomNavBar = __ds_scope.BottomNavBar;

__ds_ns.TopAppBar = __ds_scope.TopAppBar;

__ds_ns.Card = __ds_scope.Card;

})();
