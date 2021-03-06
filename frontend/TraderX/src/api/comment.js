import request from '@/utils/request'

export function getCommentList(equipmentCode) {
  return request({
    url: '/comment/equipment/' + equipmentCode,
    method: 'get'
  })
}

export function deleteComment(commentId) {
    return request({
        url: '/comment/equipment/delete/' + commentId,
        method: 'delete',
    })
}

// data: {comment = 'str'}
export function editComment(id, data) {
    return request({
        url: '/comment/equipment/edit/' + id,
        method: 'post',
        data,
    })
}

// data = {comment = 'str'}
export function postComment(code, data) {
    return request({
        url: '/comment/equipment/post/' + code,
        method: 'post',
        data
    })
}

export function revokeVote(commentId) {
    console.log('commentId in revokeVote: ' + commentId)
    return request({
        url: '/comment/equipment/revoke/' + commentId,
        method: 'delete',
    })
}

export function voteComment(commentId, voteType) {
    return request({
        url: '/comment/equipment/vote/' + commentId + '/' + voteType,
        method: 'post',
    })
}