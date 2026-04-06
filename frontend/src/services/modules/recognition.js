import service from '../api';

export function uploadImage(data) {
  return service({
    url: '/recognition/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}

export function batchUploadImages(data) {
  return service({
    url: '/recognition/batch-upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}

export function getRecognitionRecords(params) {
  return service({
    url: '/recognition/records',
    method: 'get',
    params,
  });
}

export function getRecordReport(id) {
  return service({
    url: `/recognition/records/${id}/report`,
    method: 'get',
  });
}

export function getIndividuals(params) {
  return service({
    url: '/recognition/individuals',
    method: 'get',
    params,
  });
}

export function getIndividualReport(individualId) {
  return service({
    url: `/recognition/individuals/${individualId}/report`,
    method: 'get',
  });
}
