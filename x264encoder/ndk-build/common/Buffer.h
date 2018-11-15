#pragma once
#ifndef __BUFFER_H__
#define __BUFFER_H__

#include <stdlib.h>
#include <string.h>

#ifdef WIN32
#include <malloc.h>
#endif

typedef unsigned char  BYTE;
typedef unsigned long  DWORD;
class CBuffer
{
public:
	CBuffer(void)
	{
		m_pBuf = NULL;
		m_nBufSize = 0;
		m_nCurWritePos = 0;
		m_nCurReadPos = 0;
	}

	CBuffer(int nBufSize)
	{
		Init(nBufSize);
	}

	~CBuffer(void)
	{
		if(NULL != m_pBuf)
		{
			free(m_pBuf);
			m_pBuf = NULL;
		}
	}

public:
	int Init(int nBufSize)
	{
		if(NULL != m_pBuf)
		{
			free(m_pBuf);
			m_pBuf = NULL;
		}

		if(nBufSize > 0) 
		{
			m_pBuf = (char *)malloc(nBufSize);

			if(NULL == m_pBuf)
				return (-1);

			m_nBufSize = nBufSize;
			m_nCurWritePos = 0;
			m_nCurReadPos = 0;
		}

		return (1);
	}

	void Reset(void)
	{
		m_nCurReadPos = 0;
		m_nCurWritePos = 0;
	}

	int Add(char* pData, int nDataLen)
	{
		if(NULL == m_pBuf)
			return (-1);

		if(NULL == pData || nDataLen <= 0)
			return (0);

		if(m_nCurWritePos + nDataLen > m_nBufSize)
		{
			char* pNewBuf = (char *)realloc(m_pBuf, m_nCurWritePos + nDataLen);
			
			if(NULL == pNewBuf)
				return (-1);

			m_pBuf = pNewBuf;
			m_nBufSize = m_nCurWritePos + nDataLen;
		}

		memcpy(m_pBuf + m_nCurWritePos, pData, nDataLen);
		m_nCurWritePos += nDataLen;

		return nDataLen;
	}

	int GetDataLen()  
	{
		return (m_nCurWritePos - m_nCurReadPos);  
	}

	char* GetDataPtr()
	{
		if(NULL == m_pBuf)
			return NULL;

		return (m_pBuf + m_nCurReadPos);
	}

	void SetUsed(int nUsedLen)
	{
		if(NULL == m_pBuf)
			return;

		m_nCurReadPos += nUsedLen;

		if(m_nCurReadPos >= m_nCurWritePos)
		{
			Reset();
			return;
		}

		int nLeftLen = GetDataLen();
		memcpy(m_pBuf, m_pBuf + m_nCurReadPos, nLeftLen);
			
		m_nCurReadPos = 0;
		m_nCurWritePos = nLeftLen;
	} 

private:	
	char* m_pBuf;
	int m_nBufSize;
	int m_nCurWritePos;
	int m_nCurReadPos;
};

#endif // __BUFFER_H__
