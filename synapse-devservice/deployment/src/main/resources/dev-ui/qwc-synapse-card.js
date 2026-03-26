import { LitElement, html, css } from 'lit';
import { synapseUrl } from 'build-time-data';
import { elementUrl } from 'build-time-data';

export class QwcSynapseCard extends LitElement {

    static styles = css`
        .synapse-info {
            padding: 10px;
            font-family: monospace;
        }
        .section {
            margin-bottom: 15px;
        }
        .label {
            font-weight: bold;
            color: var(--lumo-primary-text-color);
        }
        .value {
            margin-left: 10px;
        }
        a {
            color: var(--lumo-primary-color);
        }
        h3 {
            margin: 0 0 5px 0;
            color: var(--lumo-primary-text-color);
        }
    `;

    render() {
        return html`
            <div class="synapse-info">
                <div class="section">
                    <h3>Synapse Homeserver</h3>
                    <div><span class="label">URL:</span> <a href="${synapseUrl}" target="_blank">${synapseUrl}</a></div>
                </div>
                <div class="section">
                    <h3>Element Web</h3>
                    <div><span class="label">URL:</span> <a href="${elementUrl}" target="_blank">${elementUrl}</a></div>
                </div>
                <div class="section">
                    <h3>Admin Account</h3>
                    <div><span class="label">Username:</span> <span class="value">admin</span></div>
                    <div><span class="label">Password:</span> <span class="value">admin</span></div>
                </div>
                <div class="section">
                    <h3>Bot Account</h3>
                    <div><span class="label">Username:</span> <span class="value">johnnybot</span></div>
                    <div><span class="label">Password:</span> <span class="value">botpassword</span></div>
                </div>
            </div>
        `;
    }
}

customElements.define('qwc-synapse-card', QwcSynapseCard);
