<%@ page contentType="text/html; charset=UTF-8" %>
<!doctype html>
<html>
<head>
  <meta charset="utf-8"/>
  <title>Monitoring</title>

  <link href="assets/bootstrap.min.css" rel="stylesheet"/>
  <script src="assets/chart.umd.min.js"></script>
</head>
<body class="p-3">

<div class="container-fluid">
  <h3 class="mb-3">Monitoring</h3>

  <div class="row g-3 mb-3">
    <div class="col-md-3">
      <div class="card"><div class="card-body">
        <div class="text-muted">Requests (24h)</div>
        <div id="kpiRequests" class="fs-3">-</div>
      </div></div>
    </div>
    <div class="col-md-3">
      <div class="card"><div class="card-body">
        <div class="text-muted">Errors (24h)</div>
        <div id="kpiErrors" class="fs-3">-</div>
      </div></div>
    </div>
    <div class="col-md-3">
      <div class="card"><div class="card-body">
        <div class="text-muted">Avg ms (24h)</div>
        <div id="kpiAvg" class="fs-3">-</div>
      </div></div>
    </div>
  </div>

  <div class="card mb-3">
    <div class="card-body">
      <div class="d-flex justify-content-between align-items-center mb-2">
        <div class="fw-semibold">Requests / minute (last 60 min)</div>
        <button class="btn btn-sm btn-outline-primary" onclick="loadAll()">Refresh</button>
      </div>
      <canvas id="trendChart" height="90"></canvas>
    </div>
  </div>

  <div class="card">
    <div class="card-body">
      <div class="fw-semibold mb-2">Latest requests</div>
      <div class="table-responsive">
        <table class="table table-sm table-hover align-middle">
          <thead>
          <tr>
            <th>Time (UTC)</th>
            <th>Method</th>
            <th>Path</th>
            <th>Status</th>
            <th>Ms</th>
            <th>User</th>
            <th>Correlation</th>
          </tr>
          </thead>
          <tbody id="latestBody"></tbody>
        </table>
      </div>
    </div>
  </div>

</div>

<script>
let trendChart;
const TOKEN = "TINEXT_TOKEN_2025!";

async function jget(url){
  const r = await fetch(url, {
    headers: { "Authorization": "Bearer " + TOKEN }
  });
  if(!r.ok) throw new Error("HTTP " + r.status);
  return await r.json();
}


function fmtIso(iso){
  if(!iso) return '';
  return iso.replace('T',' ').replace('Z','');
}

async function loadKpi(){
  const k = await jget('monitoring/api/kpi?hours=24');
  document.getElementById('kpiRequests').textContent = k.total_requests ?? '-';
  document.getElementById('kpiErrors').textContent = k.total_errors ?? '-';
  document.getElementById('kpiAvg').textContent = Math.round(k.avg_ms ?? 0);
}

async function loadTrend(){
  const rows = await jget('monitoring/api/trend?minutes=60');
  const labels = rows.map(x => fmtIso(x.t_min).slice(11,16)); // HH:mm
  const hits = rows.map(x => x.hits);
  const errors = rows.map(x => x.errors);

  const ctx = document.getElementById('trendChart');

  if(trendChart) trendChart.destroy();
  trendChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels,
      datasets: [
        { label: 'Hits', data: hits },
        { label: 'Errors', data: errors }
      ]
    },
    options: {
      responsive: true,
      animation: false
    }
  });
}

async function loadLatest(){
  const rows = await jget('monitoring/api/latest?limit=50');
  const tb = document.getElementById('latestBody');
  tb.innerHTML = '';

  for(const r of rows){
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${fmtIso(r.ts_start)}</td>
      <td>${r.method ?? ''}</td>
      <td class="text-truncate" style="max-width:520px">${r.path ?? ''}</td>
      <td>${r.status_code ?? ''}</td>
      <td>${r.duration_ms ?? ''}</td>
      <td>${r.actor_username ?? ''}</td>
      <td class="text-truncate" style="max-width:320px">${r.correlation_id ?? ''}</td>
    `;
    tb.appendChild(tr);
  }
}

async function loadAll(){
  try{
    await loadKpi();
    await loadTrend();
    await loadLatest();
  }catch(e){
    console.error(e);
    alert('Error loading monitoring data (check console/server logs).');
  }
}

loadAll();
</script>

</body>
</html>