<ul class="nav nav-tabs" role="tablist">
  ${foreach tabView.tabs tab}
    <li role="presentation" class="${if tab.allTab}active${end}">
      <a href="#tab${tab.index}" role="tab" data-toggle="tab">
        ${if tab.allTab}
          All Features
        ${else}
          ${tab.label}
        ${end}
      </a>
    </li>
  ${end}
</ul>

<div class="tab-content">

  ${foreach tabView.tabs tab}
    <div class="tab-pane fade ${if tab.allTab}active in${end}" id="tab${tab.index}" role="tabpanel">
      <table class="table table-striped feature-overview">
        <thead>
          <tr>
            <th class="feature-label">Feature</th>
            <th class="feature-status">Status</th>
            <th class="feature-users">Strategy</th>
            <th class="feature-actions">Actions</th>
          </tr>
        </thead>
        <tbody>

          ${foreach tab.rows feature}
            <tr>
              <td class="feature-label">
                <span title="${feature.name}">${feature.label}</span>
                ${if feature.hasInfoLink}
                  <a href="${feature.infoLink}" title="Info-Link">
                    <span class="glyphicon glyphicon-info-sign text-primary"></span>
                  </a>
                ${end}
                ${if feature.hasAttributes}
                  <ul class="attributes">
                  ${foreach feature.attributes attr}
                    <li>${attr.key}: ${attr.value}</li>
                  ${end}
                  </ul>
                ${end}
              </td>
              <td class="feature-status">
              	<form method="POST" action="edit">
                    ${foreach tokens token}
                    <input type="hidden" name="${token.name}" value="${token.value}"/>
                    ${end}
                    <input type="hidden" name="f" value="${feature.name}">
                    <input type="hidden" name="enabled" value="${if feature.enabled}${else}enabled${end}">
                    <input type="submit" class="btn btn-sm btn-${if feature.enabled}success${else}danger${end}" value="${if feature.enabled}Enabled${else}Disabled${end}">
              	</form>
              </td>
              <td class="feature-strategy">
                ${feature.strategy.label}
                ${if feature.strategy.hasParametersWithValues}
                  <ul class="params">
                  ${foreach feature.strategy.parameters param}
                    ${if param.hasValue}
                      <li>${param.label}: ${param.value}</li>
                    ${end}
                  ${end}
                  </ul>
                ${end}
              </td>
              <td class="feature-actions">
                  <a class="btn btn-sm btn-default" href="edit?f=${feature.name}${foreach tokens token}${if token.name = "togglz_csrf"}&${token.name}=${token.value}${end}${end}" title="Edit ${feature.label} feature">                  <span class="glyphicon glyphicon-cog text-muted"></span>
                </a>
              </td>
            </tr>
          ${end}

        </tbody>
      </table>
    </div>
  ${end}

</div>
