/**
 * Student Achievement Portal — Progressive Vanilla JavaScript
 * Zero-framework, lightweight helpers for dynamic forms, camera capture & validation
 */

document.addEventListener('DOMContentLoaded', () => {
    initCategoryFieldSwitcher();
    initFileUploadInspectors();
    initDecisionHelper();
});

/**
 * Dynamically resizes and shows category-specific fields based on selection
 */
function initCategoryFieldSwitcher() {
    const categorySelect = document.getElementById('categorySelect');
    if (!categorySelect) return;

    const sections = {
        'HACKATHON': document.getElementById('meta-hackathon'),
        'SPORTS': document.getElementById('meta-sports'),
        'PUBLICATION': document.getElementById('meta-publication'),
        'INTERNSHIP': document.getElementById('meta-internship'),
        'COMPETITION': document.getElementById('meta-competition'),
        'CERTIFICATE': document.getElementById('meta-certificate')
    };

    function updateFields() {
        const selected = categorySelect.value;
        Object.keys(sections).forEach(code => {
            const el = sections[code];
            if (el) {
                if (code === selected) {
                    el.style.display = 'block';
                    // enable required inputs inside if needed
                    el.querySelectorAll('input, select').forEach(i => i.removeAttribute('disabled'));
                } else {
                    el.style.display = 'none';
                    el.querySelectorAll('input, select').forEach(i => i.setAttribute('disabled', 'true'));
                }
            }
        });
    }

    categorySelect.addEventListener('change', updateFields);
    updateFields(); // initial call
}

/**
 * Checks file size and type in the browser before upload, computes client SHA-256
 */
function initFileUploadInspectors() {
    const fileInputs = document.querySelectorAll('.proof-file-input');
    fileInputs.forEach(input => {
        input.addEventListener('change', async (e) => {
            const files = e.target.files;
            if (!files || files.length === 0) return;

            const file = files[0];
            const maxSize = 25 * 1024 * 1024; // 25MB

            if (file.size > maxSize) {
                alert(`File "${file.name}" exceeds the 25MB limit. Please upload a compressed document or photo.`);
                e.target.value = '';
                return;
            }

            // Visual feedback
            const container = input.closest('.upload-zone');
            if (container) {
                const titleEl = container.querySelector('.upload-title');
                if (titleEl) {
                    titleEl.textContent = `Selected: ${file.name} (${(file.size / (1024 * 1024)).toFixed(2)} MB)`;
                }
                container.style.borderColor = 'var(--accent-emerald)';
                container.style.backgroundColor = '#ecfdf5';
            }

            // Real-time client SHA-256 preview using Web Crypto API
            if (window.crypto && crypto.subtle) {
                try {
                    const buffer = await file.arrayBuffer();
                    const hashBuffer = await crypto.subtle.digest('SHA-256', buffer);
                    const hashArray = Array.from(new Uint8Array(hashBuffer));
                    const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
                    console.log(`[File Vault Pre-Flight] Checksum SHA-256 for ${file.name}: ${hashHex}`);
                } catch (err) {
                    console.warn('WebCrypto hash calculation skipped:', err);
                }
            }
        });
    });
}

/**
 * Interactive helper for faculty verification form
 */
function initDecisionHelper() {
    const decisionRadios = document.querySelectorAll('input[name="decision"]');
    const remarksField = document.getElementById('remarksInput');
    const remarksContainer = document.getElementById('remarksGroup');

    if (!decisionRadios.length || !remarksField) return;

    function handleDecisionChange() {
        const checked = document.querySelector('input[name="decision"]:checked');
        if (!checked) return;

        if (checked.value === 'APPROVED') {
            remarksField.removeAttribute('required');
            if (remarksContainer) remarksContainer.querySelector('label small').textContent = '(Optional for approval)';
        } else {
            remarksField.setAttribute('required', 'true');
            if (remarksContainer) remarksContainer.querySelector('label small').textContent = '(Mandatory for rejection or return)';
        }
    }

    decisionRadios.forEach(radio => radio.addEventListener('change', handleDecisionChange));
    handleDecisionChange();
}

/**
 * Print public profile resume
 */
function printPortfolio() {
    window.print();
}
