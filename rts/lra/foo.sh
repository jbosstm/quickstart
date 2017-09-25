oc create -f - << EOF!
apiVersion: v1
kind: PersistentVolume
metadata:
  generateName: pv-
spec:
  capacity:
    storage: 2Gi
  accessModes:
    - ReadWriteOnce
  persistentVolumeReclaimPolicy: Recycle
EOF!