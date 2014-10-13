#ifndef DDEBUG
#define DDEBUG 0
#define TTPOD_MAX_PER_LOG_LEN 100*1024
#endif
#include "ddebug.h"

#include "ngx_http_echo_echo.h"
#include "ngx_http_echo_util.h"
#include "ngx_http_echo_filter.h"

#include <nginx.h>
#include <zlib.h>

static int httpgzdecompress(Byte *zdata, uLong nzdata,Byte *data, uLong *ndata);

static ngx_buf_t ngx_http_echo_space_buf;

static ngx_buf_t ngx_http_echo_newline_buf;


ngx_int_t
ngx_http_echo_echo_init(ngx_conf_t *cf)
{
    static u_char space_str[]   = " ";
    static u_char newline_str[] = "\n";

    dd("global init...");

    ngx_memzero(&ngx_http_echo_space_buf, sizeof(ngx_buf_t));

    ngx_http_echo_space_buf.memory = 1;

    ngx_http_echo_space_buf.start =
        ngx_http_echo_space_buf.pos =
            space_str;

    ngx_http_echo_space_buf.end =
        ngx_http_echo_space_buf.last =
            space_str + sizeof(space_str) - 1;

    ngx_memzero(&ngx_http_echo_newline_buf, sizeof(ngx_buf_t));

    ngx_http_echo_newline_buf.memory = 1;

    ngx_http_echo_newline_buf.start =
        ngx_http_echo_newline_buf.pos =
            newline_str;

    ngx_http_echo_newline_buf.end =
        ngx_http_echo_newline_buf.last =
            newline_str + sizeof(newline_str) - 1;

    return NGX_OK;
}


ngx_int_t
ngx_http_echo_exec_echo_sync(ngx_http_request_t *r,
        ngx_http_echo_ctx_t *ctx)
{
    ngx_buf_t                   *buf;
    ngx_chain_t                 *cl  = NULL; /* the head of the chain link */

    buf = ngx_calloc_buf(r->pool);
    if (buf == NULL) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    buf->sync = 1;

    cl = ngx_alloc_chain_link(r->pool);
    if (cl == NULL) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    cl->buf  = buf;
    cl->next = NULL;

    return ngx_http_echo_send_chain_link(r, ctx, cl);
}


ngx_int_t
ngx_http_echo_exec_echo(ngx_http_request_t *r,
        ngx_http_echo_ctx_t *ctx, ngx_array_t *computed_args,
        ngx_flag_t in_filter, ngx_array_t *opts)
{
    ngx_uint_t                  i;

    ngx_buf_t                   *space_buf;
    ngx_buf_t                   *newline_buf;
    ngx_buf_t                   *buf;

    ngx_str_t                   *computed_arg;
    ngx_str_t                   *computed_arg_elts;
    ngx_str_t                   *opt;

    ngx_chain_t *cl  = NULL; /* the head of the chain link */
    ngx_chain_t **ll = &cl;  /* always point to the address of the last link */


    dd_enter();

    if (computed_args == NULL) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    computed_arg_elts = computed_args->elts;
    for (i = 0; i < computed_args->nelts; i++) {
        computed_arg = &computed_arg_elts[i];

        if (computed_arg->len == 0) {
            buf = NULL;

        } else {
            buf = ngx_calloc_buf(r->pool);
            if (buf == NULL) {
                return NGX_HTTP_INTERNAL_SERVER_ERROR;
            }

            buf->start = buf->pos = computed_arg->data;
            buf->last = buf->end = computed_arg->data +
                computed_arg->len;

            buf->memory = 1;
        }

        if (cl == NULL) {
            cl = ngx_alloc_chain_link(r->pool);
            if (cl == NULL) {
                return NGX_HTTP_INTERNAL_SERVER_ERROR;
            }
            cl->buf  = buf;
            cl->next = NULL;
            ll = &cl->next;

        } else {
            /* append a space first */
            *ll = ngx_alloc_chain_link(r->pool);

            if (*ll == NULL) {
                return NGX_HTTP_INTERNAL_SERVER_ERROR;
            }

            space_buf = ngx_calloc_buf(r->pool);

            if (space_buf == NULL) {
                return NGX_HTTP_INTERNAL_SERVER_ERROR;
            }

            /* nginx clears buf flags at the end of each request handling,
             * so we have to make a clone here. */
            *space_buf = ngx_http_echo_space_buf;

            (*ll)->buf = space_buf;
            (*ll)->next = NULL;

            ll = &(*ll)->next;

            /* then append the buf only if it's non-empty */
            if (buf) {
                *ll = ngx_alloc_chain_link(r->pool);
                if (*ll == NULL) {
                    return NGX_HTTP_INTERNAL_SERVER_ERROR;
                }
                (*ll)->buf  = buf;
                (*ll)->next = NULL;

                ll = &(*ll)->next;
            }
        }
    } /* end for */

    if (opts && opts->nelts > 0) {
        opt = opts->elts;
        if (opt[0].len == 1 && opt[0].data[0] == 'n') {
            goto done;
        }
    }

    /* append the newline character */

    if (cl && cl->buf == NULL) {
        cl = cl->next;
    }

    newline_buf = ngx_calloc_buf(r->pool);

    if (newline_buf == NULL) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    *newline_buf = ngx_http_echo_newline_buf;

    if (cl == NULL) {
        cl = ngx_alloc_chain_link(r->pool);

        if (cl == NULL) {
            return NGX_HTTP_INTERNAL_SERVER_ERROR;
        }

        cl->buf = newline_buf;
        cl->next = NULL;
        /* ll = &cl->next; */

    } else {
        *ll = ngx_alloc_chain_link(r->pool);

        if (*ll == NULL) {
            return NGX_HTTP_INTERNAL_SERVER_ERROR;
        }

        (*ll)->buf  = newline_buf;
        (*ll)->next = NULL;
        /* ll = &(*ll)->next; */
    }

done:

    if (cl == NULL || cl->buf == NULL) {
        return NGX_OK;
    }

    if (in_filter) {
        return ngx_http_echo_next_body_filter(r, cl);
    }

    return ngx_http_echo_send_chain_link(r, ctx, cl);
}


ngx_int_t
ngx_http_echo_exec_echo_flush(ngx_http_request_t *r, ngx_http_echo_ctx_t *ctx)
{
    return ngx_http_send_special(r, NGX_HTTP_FLUSH);
}


ngx_int_t
ngx_http_echo_exec_echo_request_body(ngx_http_request_t *r,
        ngx_http_echo_ctx_t *ctx)
{
    if (r->request_body && r->request_body->bufs) {
        return ngx_http_echo_send_chain_link(r, ctx, r->request_body->bufs);
    }

    return NGX_OK;
}

ngx_int_t
ngx_http_echo_exec_echo_stat(ngx_http_request_t *r,
	ngx_http_echo_ctx_t *ctx)
{
    ngx_buf_t *b;
    u_char *p;
    u_char *de_data;
    size_t  len;
    ngx_buf_t *buf;
    ngx_chain_t *cl;
    unsigned long outlen;
    ngx_str_t ok = ngx_string("{\"code\":1,\"msg\":\"ok\"}");
    ngx_str_t error = ngx_string("{\"code\":0,\"msg\":\"error\"}");
    if (r->request_body && r->request_body->bufs) {
        outlen = TTPOD_MAX_PER_LOG_LEN;
	cl = r->request_body->bufs;
	buf = cl->buf;
	if (cl->next == NULL){
	    if((buf->last - buf->pos) > 8 ){
	    	if(ngx_strncmp(buf->pos,"{\"data\":",8) != 0){
			de_data = ngx_palloc(r->pool,TTPOD_MAX_PER_LOG_LEN);
			if(de_data == NULL){
				return NGX_HTTP_INTERNAL_SERVER_ERROR;		
			}
			if(httpgzdecompress((Byte*)buf->pos,(buf->last - buf->pos),(Byte*)de_data,&outlen) != 0){
				b = ngx_create_temp_buf(r->pool,error.len);
				if(b == NULL){
					return NGX_HTTP_INTERNAL_SERVER_ERROR;
				}
				ngx_memcpy(b->pos,error.data,error.len);
				b->last = b->pos + error.len;
				b->last_buf = 1;
				ngx_chain_t out;
				out.buf = b;
				out.next = NULL;
        			return ngx_http_echo_send_chain_link(r, ctx, &out);
			}
			else{
                                b = ngx_create_temp_buf(r->pool,ok.len);
                                if(b == NULL){
                                        return NGX_HTTP_INTERNAL_SERVER_ERROR;
                                }
                                ngx_memcpy(b->pos,ok.data,ok.len);
                                b->last = b->pos + ok.len;
                                b->last_buf = 1;
                                ngx_chain_t out;
                                out.buf = b;
                                out.next = NULL;
                                return ngx_http_echo_send_chain_link(r, ctx, &out);
                        }
		}
		else{
                        b = ngx_create_temp_buf(r->pool,ok.len);
                        if(b == NULL){
                                return NGX_HTTP_INTERNAL_SERVER_ERROR;
                        }
                        ngx_memcpy(b->pos,ok.data,ok.len);
                        b->last = b->pos + ok.len;
                        b->last_buf = 1;
                        ngx_chain_t out;
                        out.buf = b;
                        out.next = NULL;
                        return ngx_http_echo_send_chain_link(r, ctx, &out);
                }
	    }	

	}    	
	len = buf->last - buf->pos;
	cl = cl->next;
	for(/*void*/;cl;cl = cl->next){
		buf = cl->buf;
		len += buf->last - buf->pos;	
	}
	p = ngx_pnalloc(r->pool,len);
	if(p == NULL){
		return NGX_HTTP_INTERNAL_SERVER_ERROR;
	}
	cl = r->request_body->bufs;
	for(/*void*/;cl;cl=cl->next){
		buf = cl->buf;
		p = ngx_cpymem(p,buf->pos,buf->last - buf->pos);
	}
	if(len > 8){
		if(ngx_strncmp(p-len,"{\"data\":",8) != 0){
			de_data = ngx_palloc(r->pool,TTPOD_MAX_PER_LOG_LEN);
			if(de_data == NULL){
				return NGX_HTTP_INTERNAL_SERVER_ERROR;
			}
			if(httpgzdecompress((Byte*)(p-len), len,(Byte*)de_data, &outlen) != 0){
				b = ngx_create_temp_buf(r->pool,error.len);
                                if(b == NULL){
                                        return NGX_HTTP_INTERNAL_SERVER_ERROR;
                                }
                                ngx_memcpy(b->pos,error.data,error.len);
                                b->last = b->pos + error.len;
                                b->last_buf = 1;
                                ngx_chain_t out;
                                out.buf = b;
                                out.next = NULL;
                                return ngx_http_echo_send_chain_link(r, ctx, &out);
			}
			else{
				b = ngx_create_temp_buf(r->pool,ok.len);
                                if(b == NULL){
                                        return NGX_HTTP_INTERNAL_SERVER_ERROR;
                                }
                                ngx_memcpy(b->pos,ok.data,ok.len);
                                b->last = b->pos + ok.len;
                                b->last_buf = 1;
                                ngx_chain_t out;
                                out.buf = b;
                                out.next = NULL;
                                return ngx_http_echo_send_chain_link(r, ctx, &out);
			}
			
		}
		else{
			b = ngx_create_temp_buf(r->pool,ok.len);
                        if(b == NULL){
                        	return NGX_HTTP_INTERNAL_SERVER_ERROR;
                        }
                        ngx_memcpy(b->pos,ok.data,ok.len);
                        b->last = b->pos + ok.len;
                        b->last_buf = 1;
                        ngx_chain_t out;
                        out.buf = b;
                        out.next = NULL;
                        return ngx_http_echo_send_chain_link(r, ctx, &out);
		}
	}
    }
    else{
	b = ngx_create_temp_buf(r->pool,error.len);
        if(b == NULL){
        	return NGX_HTTP_INTERNAL_SERVER_ERROR;
        }
        ngx_memcpy(b->pos,error.data,error.len);
        b->last = b->pos + error.len;
        b->last_buf = 1;
        ngx_chain_t out;
        out.buf = b;
    	out.next = NULL;
    	return ngx_http_echo_send_chain_link(r, ctx, &out);	
    }
    return NGX_OK;
}


ngx_int_t
ngx_http_echo_exec_echo_duplicate(ngx_http_request_t *r,
        ngx_http_echo_ctx_t *ctx, ngx_array_t *computed_args)
{
    ngx_str_t                   *computed_arg;
    ngx_str_t                   *computed_arg_elts;
    ssize_t                     i, count;
    ngx_str_t                   *str;
    u_char                      *p;
    ngx_int_t                   rc;

    ngx_buf_t                   *buf;
    ngx_chain_t                 *cl;


    dd_enter();

    computed_arg_elts = computed_args->elts;

    computed_arg = &computed_arg_elts[0];

    count = ngx_http_echo_atosz(computed_arg->data, computed_arg->len);

    if (count == NGX_ERROR) {
        ngx_log_error(NGX_LOG_ERR, r->connection->log, 0,
                   "invalid size specified: \"%V\"", computed_arg);

        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    str = &computed_arg_elts[1];

    if (count == 0 || str->len == 0) {
        rc = ngx_http_echo_send_header_if_needed(r, ctx);
        if (rc == NGX_ERROR || rc > NGX_OK || r->header_only) {
            return rc;
        }

        return NGX_OK;
    }

    buf = ngx_create_temp_buf(r->pool, count * str->len);
    if (buf == NULL) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }

    p = buf->pos;
    for (i = 0; i < count; i++) {
        p = ngx_copy(p, str->data, str->len);
    }
    buf->last = p;

    cl = ngx_alloc_chain_link(r->pool);
    if (cl == NULL) {
        return NGX_HTTP_INTERNAL_SERVER_ERROR;
    }
    cl->next = NULL;
    cl->buf = buf;

    return ngx_http_echo_send_chain_link(r, ctx, cl);
}

int httpgzdecompress(Byte *zdata, uLong nzdata,
        Byte *data, uLong *ndata)
{
    int err = 0;
    z_stream d_stream = {0}; /* decompression stream */
    static char dummy_head[2] =
    {
        0x8 + 0x7 * 0x10,
        (((0x8 + 0x7 * 0x10) * 0x100 + 30) / 31 * 31) & 0xFF,
    };
    d_stream.zalloc = (alloc_func)0;
    d_stream.zfree = (free_func)0;
    d_stream.opaque = (voidpf)0;
    d_stream.next_in  = zdata;
    d_stream.avail_in = 0;
    d_stream.next_out = data;
    if(inflateInit2(&d_stream, 47) != Z_OK) return -1;
    while (d_stream.total_out < *ndata && d_stream.total_in < nzdata) {
        d_stream.avail_in = d_stream.avail_out = 1; /* force small buffers */
        if((err = inflate(&d_stream, Z_NO_FLUSH)) == Z_STREAM_END) break;
        if(err != Z_OK )
        {
            if(err == Z_DATA_ERROR)
            {
                d_stream.next_in = (Bytef*) dummy_head;
                d_stream.avail_in = sizeof(dummy_head);
                if((err = inflate(&d_stream, Z_NO_FLUSH)) != Z_OK)
                {
                    return -1;
                }
            }
            else return -1;
        }
    }
    if(inflateEnd(&d_stream) != Z_OK) return -1;
    *ndata = d_stream.total_out;
    return 0;
}
