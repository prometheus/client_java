local dashboard = import 'dashboards/jvm_rev1.libsonnet';

{
  grafanaDashboards+:: {
    'jvm-dashboard.json': dashboard
  }
}