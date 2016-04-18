/* ------------------------------------------------------------------
 * Copyright (C) 2009 Martin Storsjo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 * -------------------------------------------------------------------
 */

#include <stdio.h>
#include <stdint.h>
#include <interf_enc.h>
#include <unistd.h>
#include <stdlib.h>

void usage(const char* name) {
	fprintf(stderr, "%s [-r bitrate] [-d] in.wav out.amr\n", name);
}

enum Mode findMode(const char* str) {
	struct {
		enum Mode mode;
		int rate;
	} modes[] = {
		{ MR475,  4750 },
		{ MR515,  5150 },
		{ MR59,   5900 },
		{ MR67,   6700 },
		{ MR74,   7400 },
		{ MR795,  7950 },
		{ MR102, 10200 },
		{ MR122, 12200 }
	};
	int rate = atoi(str);
	int closest = -1;
	int closestdiff = 0;
	unsigned int i;
	for (i = 0; i < sizeof(modes)/sizeof(modes[0]); i++) {
		if (modes[i].rate == rate)
			return modes[i].mode;
		if (closest < 0 || closestdiff > abs(modes[i].rate - rate)) {
			closest = i;
			closestdiff = abs(modes[i].rate - rate);
		}
	}
	fprintf(stderr, "Using bitrate %d\n", modes[closest].rate);
	return modes[closest].mode;
}

void *amr;
enum Mode mode;
#define FRAME_SIZE 160
static int encode = 0;

int amr_enc_init(const char*outfile, int bitsPerSample, int channels, int sampleRate) 
{
	
	int dtx = 0;
	FILE *out;
	mode = MR122;
	
	if(encode++ != 0)
        return -1;
	
	if (bitsPerSample != 16) 
	{
		fprintf(stderr, "Unsupported WAV sample depth %d\n", bitsPerSample);
		return 1;
	}
	if (channels != 1)
		fprintf(stderr, "Warning, only compressing one audio channel\n");
	if (sampleRate != 8000)
		fprintf(stderr, "Warning, AMR-NB uses 8000 Hz sample rate (WAV file has %d Hz)\n", sampleRate);

	amr = Encoder_Interface_init(dtx);
	out = fopen(outfile, "wb");
	if (!out) {
		perror(outfile);
		return 1;
	}

	//Write Amr header
	fwrite("#!AMR\n", 1, 6, out);
	fclose(out);

	return 0;
}

int handleAmrEnc(const char*outfile, int channels, short* inputBuf, int size)
{
	if (!encode)
        return -1;
	FILE *out = fopen(outfile, "ab+");
	if (!out) {
		perror(outfile);
		return 1;
	}

	uint8_t outbuf[500];
	int i, n;
	if (size < FRAME_SIZE)
		return -1;

	n = Encoder_Interface_Encode(amr, mode, inputBuf, outbuf);
	fwrite(outbuf, 1, n, out);

	fclose(out);

	return 0;
}

int arm_enc_exit()
{
	if (!encode)
        return -1;
	Encoder_Interface_exit(amr);
	encode = 0;
}
