import { LitElement, html, css } from 'lit';
import { JsonRpc } from 'jsonrpc';
import '@vaadin/details';
import '@vaadin/vertical-layout';

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

    static properties = {
        _config: { state: true }
    };

    constructor() {
        super();
        this._config = null;
        this._loadConfig();
    }

    _loadConfig() {
        // Read config from dev service properties injected into the page
        const synapseUrl = this._getDevProperty('matrix.base-url');
        const elementUrl = this._findElementUrl();
        this._config = {
            synapseUrl: synapseUrl || 'Not available',
            elementUrl: elementUrl || 'Not available',
            adminUser: 'admin',
            adminPassword: 'admin',
            botUser: 'johnnybot',
            botPassword: 'botpassword'
        };
    }

    _getDevProperty(key) {
        // DevService properties are available via meta tags or global config
        const meta = document.querySelector(`meta[name="devservice.${key}"]`);
        return meta ? meta.content : null;
    }

    _findElementUrl() {
        // Element URL is logged but not directly exposed as a property
        // Users should check the Quarkus dev log for the Element Web URL
        return null;
    }

    render() {
        return html`
            <div class="synapse-info">
                <div class="section">
                    <h3>Synapse Homeserver</h3>
                    <div><span class="label">URL:</span> <span class="value">${this._config?.synapseUrl}</span></div>
                </div>
                <div class="section">
                    <h3>Element Web</h3>
                    <div><span class="label">Check Quarkus dev log for Element Web URL</span></div>
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
