package azure.util;

import lejos.nxt.Flash;

public class NativeFlash {
	public static final int FLASH_ADDRESS = 0x00100000;
	public static final int FLASH_MAX_PAGES = 1024;
	public static final int FLASH_USER_PAGES = Flash.MAX_USER_PAGES;
	public static final int FLASH_START_PAGE = FLASH_MAX_PAGES - FLASH_USER_PAGES;
	public static final int FLASH_BASE = FLASH_ADDRESS + FLASH_START_PAGE * Flash.BYTES_PER_PAGE;

	public static final int getFlashRawAddress(int page, int offset) {
		return FLASH_BASE + page * Flash.BYTES_PER_PAGE + offset;
	}
}
